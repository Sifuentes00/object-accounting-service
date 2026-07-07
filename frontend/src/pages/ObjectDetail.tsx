import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useEditMode } from '../context/EditModeContext';
import { useAuth } from '../context/AuthContext';
import ContractModal from '../components/ContractModal';
import PprModal from '../components/PprModal';
import ObjectModal from '../components/ObjectModal';
import ConfirmModal from '../components/ConfirmModal';

interface Object {
  id: string;
  status: string;
  name: string;
  address: string;
  workType: string;
  imageUniqueName?: string;
  customer?: {
    id: string;
    name: string;
  };
  responsibleEmployee?: {
    id: string;
    fullName: string;
    position?: string;
    phoneNumber?: string;
  };
}

interface Contract {
  id: number;
  number: string;
  conclusionDate: string;
  endDate: string;
  fileUniqueName: string;
}

interface Ppr {
  id: number;
  name: string;
  archiveNumber: string;
  number: string;
  fileUniqueName: string;
  employeeId?: number;
  employeeFullName?: string;
  employeePosition?: string;
}

const formatWorkType = (workType: string): string => {
  const workTypeMap: Record<string, string> = {
    'DESIGN': 'Проектирование',
    'GEODESY': 'Геодезические работы',
    'CONSTRUCTION_INSTALLATION': 'Строительно-монтажные работы'
  };
  return workTypeMap[workType] || workType;
};

const formatStatusWithLineBreaks = (status: string) => {
  if (!status) return '';
  return status.replace(/\n/g, '<br />');
};

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('ru-RU');
};

export default function ObjectDetail() {
  const { id } = useParams<{ id: string }>();
  const { editMode } = useEditMode();
  const { role } = useAuth();
  const isAdmin = role === 'ADMIN';
  const [object, setObject] = useState<Object | null>(null);
  const [editingStatusValue, setEditingStatusValue] = useState<string>('');
  const [imageUrl, setImageUrl] = useState<string>('');
  const [contracts, setContracts] = useState<Contract[]>([]);
  const [pprs, setPprs] = useState<Ppr[]>([]);
  const [loading, setLoading] = useState(true);
  const [isContractModalOpen, setIsContractModalOpen] = useState(false);
  const [isPprModalOpen, setIsPprModalOpen] = useState(false);
  const [isObjectModalOpen, setIsObjectModalOpen] = useState(false);
  const [editingContract, setEditingContract] = useState<Contract | null>(null);
  const [editingPpr, setEditingPpr] = useState<Ppr | null>(null);
  const [employees, setEmployees] = useState<any[]>([]);
  const [deleteConfirm, setDeleteConfirm] = useState<{ isOpen: boolean; onConfirm: () => void; title: string; message: string }>({
    isOpen: false,
    onConfirm: () => {},
    title: '',
    message: ''
  });
  const [expandedFile, setExpandedFile] = useState<{ contractId?: number; pprId?: number; url?: string } | null>(null);

  useEffect(() => {
    if (id) {
      loadObject(id);
      loadContracts(id);
      loadPprs(id);
      loadEmployees();
    }
  }, [id]);

  useEffect(() => {
    if (object) {
      setEditingStatusValue(object.status || '');
    }
  }, [object]);

  const loadObject = async (objectId: string) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`/api/v1/objects/${objectId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      if (response.ok) {
        const data = await response.json();
        setObject(data);
        if (data.imageUniqueName) {
          loadImage(objectId);
        }
      }
    } catch (error) {
      console.error('Error loading object:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadImage = async (objectId: string) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`/api/v1/objects/${objectId}/image`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      if (response.ok) {
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        setImageUrl(url);
      }
    } catch (error) {
      console.error('Error loading image:', error);
    }
  };

  const loadContracts = async (objectId: string) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`/api/v1/contracts/object/${objectId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      if (response.ok) {
        const data = await response.json();
        setContracts(data);
      }
    } catch (error) {
      console.error('Error loading contracts:', error);
    }
  };

  const loadPprs = async (objectId: string) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`/api/v1/pprs/object/${objectId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      if (response.ok) {
        const data = await response.json();
        setPprs(data);
      }
    } catch (error) {
      console.error('Error loading pprs:', error);
    }
  };

  const loadEmployees = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('/api/v1/employees', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      if (response.ok) {
        const data = await response.json();
        setEmployees(data);
      }
    } catch (error) {
      console.error('Error loading employees:', error);
    }
  };

  const handleFileClick = async (contractId: number | undefined, pprId: number | undefined) => {
    if (contractId !== undefined) {
      if (expandedFile?.contractId === contractId) {
        setExpandedFile(null);
      } else {
        try {
          const token = localStorage.getItem('token');
          const response = await fetch(`/api/v1/contracts/${contractId}/file`, {
            headers: {
              'Authorization': `Bearer ${token}`,
            },
          });
          if (response.ok) {
            const blob = await response.blob();
            const url = URL.createObjectURL(blob);
            setExpandedFile({ contractId, pprId: undefined, url });
          }
        } catch (error) {
          console.error('Error loading file:', error);
        }
      }
    } else if (pprId !== undefined) {
      if (expandedFile?.pprId === pprId) {
        setExpandedFile(null);
      } else {
        try {
          const token = localStorage.getItem('token');
          const response = await fetch(`/api/v1/pprs/${pprId}/file`, {
            headers: {
              'Authorization': `Bearer ${token}`,
            },
          });
          if (response.ok) {
            const blob = await response.blob();
            const url = URL.createObjectURL(blob);
            setExpandedFile({ contractId: undefined, pprId, url });
          }
        } catch (error) {
          console.error('Error loading file:', error);
        }
      }
    }
  };

  const handleDownloadFile = async (contractId: number | undefined, pprId: number | undefined, fileName: string) => {
    try {
      const token = localStorage.getItem('token');
      let url: string;

      if (contractId !== undefined) {
        url = `/api/v1/contracts/${contractId}/file`;
      } else if (pprId !== undefined) {
        url = `/api/v1/pprs/${pprId}/file`;
      } else {
        return;
      }

      const response = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      if (response.ok) {
        const blob = await response.blob();
        const blobUrl = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = blobUrl;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(blobUrl);
      }
    } catch (error) {
      console.error('Error downloading file:', error);
    }
  };

  const handleEditObject = () => {
    setIsObjectModalOpen(true);
  };

  const handleApplyStatus = async () => {
    if (!id || !editingStatusValue) return;
    
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`/api/v1/objects/${id}/status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify({ status: editingStatusValue }),
      });
      
      if (response.ok) {
        await loadObject(id!);
      }
    } catch (error) {
      console.error('Error saving status:', error);
    }
  };

  const handleSaveObject = async (data: any) => {
    try {
      const token = localStorage.getItem('token');
      const payload = {
        name: data.name,
        address: data.address,
        workType: data.workType,
        customerId: data.customerId ? parseInt(data.customerId) : null,
        responsibleEmployeeId: data.responsibleEmployeeId ? parseInt(data.responsibleEmployeeId) : null,
      };

      const response = await fetch(`/api/v1/objects/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        if (data.image) {
          const formData = new FormData();
          formData.append('file', data.image);
          
          const imageResponse = await fetch(`/api/v1/objects/${id}/image`, {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${token}`,
            },
            body: formData,
          });
          
          if (!imageResponse.ok) {
            console.error('Failed to upload image');
          }
        }
        
        await loadObject(id!);
        setIsObjectModalOpen(false);
      }
    } catch (error) {
      console.error('Error saving object:', error);
    }
  };

  const handleAddContract = () => {
    setEditingContract(null);
    setIsContractModalOpen(true);
  };

  const handleEditContract = (contract: Contract) => {
    setEditingContract(contract);
    setIsContractModalOpen(true);
  };

  const handleSaveContract = async (data: any, file?: File | null) => {
    try {
      const token = localStorage.getItem('token');
      let response;
      let contractId;
      
      if (editingContract) {
        response = await fetch(`/api/v1/contracts/${editingContract.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
          body: JSON.stringify(data),
        });
        contractId = editingContract.id;
      } else {
        response = await fetch(`/api/v1/contracts`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
          body: JSON.stringify(data),
        });
        if (response.ok) {
          const result = await response.json();
          contractId = result.id;
        }
      }
      
      if (response.ok && file && contractId) {
        const formData = new FormData();
        formData.append('file', file);
        
        const fileResponse = await fetch(`/api/v1/contracts/${contractId}/file`, {
          method: editingContract ? 'PUT' : 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
          },
          body: formData,
        });
        
        if (fileResponse.ok) {
          await loadContracts(id!);
        }
      } else if (response.ok) {
        await loadContracts(id!);
      }
    } catch (error) {
      console.error('Error saving contract:', error);
    }
  };

  const handleDeleteContract = async (contractId: number) => {
    setDeleteConfirm({
      isOpen: true,
      onConfirm: async () => {
        try {
          const token = localStorage.getItem('token');
          const response = await fetch(`/api/v1/contracts/${contractId}`, {
            method: 'DELETE',
            headers: {
              'Authorization': `Bearer ${token}`,
            },
          });
          if (response.ok) {
            await loadContracts(id!);
          }
        } catch (error) {
          console.error('Error deleting contract:', error);
        }
        setDeleteConfirm({ isOpen: false, onConfirm: () => {}, title: '', message: '' });
      },
      title: 'Удалить договор',
      message: 'Вы уверены, что хотите удалить этот договор?'
    });
  };

  const handleAddPpr = () => {
    setEditingPpr(null);
    setIsPprModalOpen(true);
  };

  const handleEditPpr = (ppr: Ppr) => {
    setEditingPpr(ppr);
    setIsPprModalOpen(true);
  };

  const handleSavePpr = async (data: any, file?: File | null) => {
    try {
      const token = localStorage.getItem('token');
      let response;
      let pprId;

      if (editingPpr) {
        response = await fetch(`/api/v1/pprs/${editingPpr.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
          body: JSON.stringify(data),
        });
        pprId = editingPpr.id;
      } else {
        response = await fetch(`/api/v1/pprs`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
          body: JSON.stringify(data),
        });
        if (response.ok) {
          const result = await response.json();
          pprId = result.id;
        }
      }

      if (response.ok && file && pprId) {
        const formData = new FormData();
        formData.append('file', file);
        
        const fileResponse = await fetch(`/api/v1/pprs/${pprId}/file`, {
          method: editingPpr ? 'PUT' : 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
          },
          body: formData,
        });
        
        if (fileResponse.ok) {
          await loadPprs(id!);
          setIsPprModalOpen(false);
        }
      } else if (response.ok) {
        await loadPprs(id!);
        setIsPprModalOpen(false);
      }
    } catch (error) {
      console.error('Error saving ppr:', error);
    }
  };

  const handleDeletePpr = async (pprId: number) => {
    setDeleteConfirm({
      isOpen: true,
      onConfirm: async () => {
        try {
          const token = localStorage.getItem('token');
          const response = await fetch(`/api/v1/pprs/${pprId}`, {
            method: 'DELETE',
            headers: {
              'Authorization': `Bearer ${token}`,
            },
          });
          if (response.ok) {
            await loadPprs(id!);
          }
        } catch (error) {
          console.error('Error deleting ppr:', error);
        }
        setDeleteConfirm({ isOpen: false, onConfirm: () => {}, title: '', message: '' });
      },
      title: 'Удалить ППР',
      message: 'Вы уверены, что хотите удалить этот ППР?'
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="text-gray-600">Загрузка...</div>
      </div>
    );
  }

  if (!object) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="text-gray-600">Объект не найден</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-6">Детали объекта</h1>

        <div className="bg-white rounded-lg shadow-lg overflow-hidden mb-8">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 p-8">
            <div>
              {imageUrl ? (
                <img src={imageUrl} alt={object.name} className="w-full h-96 object-cover rounded-lg" />
              ) : (
                <div className="w-full h-96 bg-gray-200 flex items-center justify-center rounded-lg">
                  <span className="text-gray-400">Фото объекта</span>
                </div>
              )}
            </div>
            <div className="flex flex-col justify-center">
              <h1 className="text-4xl font-bold text-gray-900 mb-6">{object.name}</h1>
              <div className="space-y-4">
                <div className="flex items-start">
                  <span className="text-gray-600 font-medium w-40 shrink-0">Адрес:</span>
                  <span className="text-gray-800">{object.address}</span>
                </div>
                <div className="flex items-start">
                  <span className="text-gray-600 font-medium w-40 shrink-0">Тип работ:</span>
                  <span className="text-gray-800">{formatWorkType(object.workType)}</span>
                </div>
                <div className="flex items-start">
                  <span className="text-gray-600 font-medium w-40 shrink-0">Текущее состояние:</span>
                  {editMode && isAdmin ? (
                    <div className="flex-1">
                      <textarea
                        value={editingStatusValue}
                        onChange={(e) => setEditingStatusValue(e.target.value)}
                        className="w-full border border-gray-300 rounded px-3 py-2 resize-y"
                        rows={4}
                        placeholder="Введите текущее состояние..."
                      />
                      <button
                        onClick={handleApplyStatus}
                        className="mt-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
                      >
                        Применить
                      </button>
                    </div>
                  ) : (
                    <div
                      className="text-gray-800"
                      dangerouslySetInnerHTML={{ __html: formatStatusWithLineBreaks(object.status) }}
                    />
                  )}
                </div>
                {object.customer && (
                  <div className="flex items-start">
                    <span className="text-gray-600 font-medium w-40 shrink-0">Заказчик:</span>
                    <span className="text-gray-800">{object.customer.name}</span>
                  </div>
                )}
                {object.responsibleEmployee && (
                  <div className="flex items-start">
                    <span className="text-gray-600 font-medium w-40 shrink-0">Ответственный:</span>
                    <div className="text-gray-800">
                      <div className="font-medium">{object.responsibleEmployee.fullName}</div>
                      {object.responsibleEmployee.position && (
                        <div className="text-sm text-gray-600">{object.responsibleEmployee.position}</div>
                      )}
                      {object.responsibleEmployee.phoneNumber && (
                        <div className="text-sm text-gray-600">{object.responsibleEmployee.phoneNumber}</div>
                      )}
                    </div>
                  </div>
                )}
              </div>
              {editMode && (
                <button
                  onClick={handleEditObject}
                  className="mt-6 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  Редактировать
                </button>
              )}
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-lg overflow-hidden mb-8">
          <div className="p-6 border-b flex justify-between items-center">
            <h2 className="text-2xl font-bold text-gray-900">Договор</h2>
            {editMode && isAdmin && contracts.length < 1 && (
              <button
                onClick={handleAddContract}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Добавить договор
              </button>
            )}
          </div>
          <div className="p-6">
            {contracts.length > 0 ? (
              <div className="space-y-4">
                {contracts.map((contract) => (
                  <div
                    key={contract.id}
                    className="flex flex-col items-start justify-between p-4 border rounded-lg hover:bg-gray-50 cursor-pointer"
                    onClick={() => handleFileClick(contract.id, undefined)}
                  >
                    <div className="flex items-center justify-between w-full">
                      <div className="flex-1">
                        <div className="font-medium text-gray-900">Договор №{contract.number}</div>
                        <div className="text-sm text-gray-600">
                          с {formatDate(contract.conclusionDate)} по {formatDate(contract.endDate)}
                        </div>
                      </div>
                      <div className="flex gap-2">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDownloadFile(contract.id, undefined, `Договор ${contract.number}.pdf`);
                          }}
                          className="ml-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                        >
                          Скачать
                        </button>
                      {editMode && isAdmin && (
                        <>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleEditContract(contract);
                            }}
                            className="px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700"
                          >
                            Редактировать
                          </button>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleDeleteContract(contract.id);
                            }}
                            className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
                          >
                            Удалить
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                  {expandedFile?.contractId === contract.id && expandedFile.url && (
                    <div className="mt-4 border rounded-lg overflow-hidden bg-gray-100 p-4">
                      <div className="w-full h-[500px] flex items-center justify-center overflow-hidden">
                        <iframe
                          src={expandedFile.url}
                          style={{ 
                            width: '140%',
                            height: '600px',
                            transform: 'scale(1.2)',
                            transformOrigin: 'center center'
                          }}
                          title={`Договор ${contract.number}`}
                        />
                      </div>
                    </div>
                  )}
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-gray-500 text-center py-4">Нет договоров</div>
            )}
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-lg overflow-hidden mb-8">
          <div className="p-6 border-b flex justify-between items-center">
            <h2 className="text-2xl font-bold text-gray-900">ППР</h2>
            {editMode && isAdmin && (
              <button
                onClick={handleAddPpr}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Добавить ППР
              </button>
            )}
          </div>
          <div className="p-6">
            {pprs.length > 0 ? (
              <div className="space-y-4">
                {pprs.map((ppr) => (
                  <div
                    key={ppr.id}
                    className="flex flex-col items-start justify-between p-4 border rounded-lg hover:bg-gray-50 cursor-pointer"
                    onClick={() => handleFileClick(undefined, ppr.id)}
                  >
                    <div className="flex items-center justify-between w-full">
                      <div className="flex-1">
                        <div className="font-medium text-gray-900">{ppr.name}</div>
                        <div className="text-sm text-gray-600">
                          Арх. №{ppr.archiveNumber} | №{ppr.number}
                          {ppr.employeeFullName && ` | Разработчик: ${ppr.employeeFullName} (${ppr.employeePosition})`}
                        </div>
                      </div>
                      <div className="flex gap-2">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDownloadFile(undefined, ppr.id, `${ppr.name}.pdf`);
                          }}
                          className="ml-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                        >
                          Скачать
                        </button>
                      {editMode && isAdmin && (
                        <>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleEditPpr(ppr);
                            }}
                            className="px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700"
                          >
                            Редактировать
                          </button>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleDeletePpr(ppr.id);
                            }}
                            className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
                          >
                            Удалить
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                  {expandedFile?.pprId === ppr.id && expandedFile.url && (
                    <div className="mt-4 border rounded-lg overflow-hidden bg-gray-100 p-4">
                      <div className="w-full h-[500px] flex items-center justify-center overflow-hidden">
                        <iframe
                          src={expandedFile.url}
                          style={{ 
                            width: '140%',
                            height: '600px',
                            transform: 'scale(1.2)',
                            transformOrigin: 'center center'
                          }}
                          title={ppr.name}
                        />
                      </div>
                    </div>
                  )}
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-gray-500 text-center py-4">Нет ППР</div>
            )}
          </div>
        </div>

        <ObjectModal
          isOpen={isObjectModalOpen}
          onClose={() => setIsObjectModalOpen(false)}
          onSave={handleSaveObject}
          object={object}
        />

        <ContractModal
          isOpen={isContractModalOpen}
          onClose={() => setIsContractModalOpen(false)}
          onSave={handleSaveContract}
          contract={editingContract}
          objectId={id!}
        />

        <PprModal
          isOpen={isPprModalOpen}
          onClose={() => setIsPprModalOpen(false)}
          onSave={handleSavePpr}
          ppr={editingPpr}
          objectId={id!}
          employees={employees}
        />

        <ConfirmModal
          isOpen={deleteConfirm.isOpen}
          onClose={() => setDeleteConfirm({ isOpen: false, onConfirm: () => {}, title: '', message: '' })}
          onConfirm={deleteConfirm.onConfirm}
          title={deleteConfirm.title}
          message={deleteConfirm.message}
        />
      </div>
    </div>
  );
}
