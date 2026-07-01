import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useEditMode } from '../context/EditModeContext';
import ObjectModal from '../components/ObjectModal';

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
  createdAt?: string;
  updatedAt?: string;
}

const formatWorkType = (workType: string): string => {
  const workTypeMap: Record<string, string> = {
    'DESIGN': 'Проектирование',
    'GEODESY': 'Геодезические работы',
    'CONSTRUCTION_INSTALLATION': 'Строительно-монтажные работы'
  };
  console.log('workType:', workType, 'mapped:', workTypeMap[workType]);
  return workTypeMap[workType] || workType;
};

export default function Objects() {
  const { role } = useAuth();
  const { editMode } = useEditMode();
  const isAdmin = role === 'ADMIN';
  const [objects, setObjects] = useState<Object[]>([]);
  const [editingStatusId, setEditingStatusId] = useState<string | null>(null);
  const [editingStatusValue, setEditingStatusValue] = useState<string>('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingObject, setEditingObject] = useState<Object | null>(null);
  const [imageUrls, setImageUrls] = useState<Record<string, string>>({});

  useEffect(() => {
    loadObjects();
  }, []);

  useEffect(() => {
    objects.forEach(obj => {
      if (obj.imageUniqueName && !imageUrls[obj.id]) {
        loadImage(obj.id);
      }
    });
  }, [objects]);

  const loadImage = async (objectId: string) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8090/api/v1/objects/${objectId}/image`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      if (response.ok) {
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        setImageUrls(prev => ({ ...prev, [objectId]: url }));
      }
    } catch (error) {
      console.error('Error loading image:', error);
    }
  };

  const loadObjects = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8090/api/v1/objects', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      if (response.ok) {
        const data = await response.json();
        console.log('Loaded objects:', data);
        setObjects(data);
      }
    } catch (error) {
      console.error('Error loading objects:', error);
    }
  };

  const handleSaveObject = async (data: any) => {
    try {
      const token = localStorage.getItem('token');
      const payload = {
        name: data.name,
        status: data.status,
        address: data.address,
        workType: data.workType,
        customerId: data.customerId ? parseInt(data.customerId) : null,
        responsibleEmployeeId: data.responsibleEmployeeId ? parseInt(data.responsibleEmployeeId) : null,
      };

      let response;
      let objectId;

      if (editingObject) {
        response = await fetch(`http://localhost:8090/api/v1/objects/${editingObject.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
          body: JSON.stringify(payload),
        });
        objectId = editingObject.id;
      } else {
        response = await fetch('http://localhost:8090/api/v1/objects', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
          body: JSON.stringify(payload),
        });
        if (response.ok) {
          const result = await response.json();
          objectId = result.id;
        }
      }

      if (response.ok) {
        if (data.image && objectId) {
          const formData = new FormData();
          formData.append('file', data.image);
          
          console.log('Uploading image for object:', objectId, 'file:', data.image.name);
          const imageResponse = await fetch(`http://localhost:8090/api/v1/objects/${objectId}/image`, {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${token}`,
            },
            body: formData,
          });
          
          console.log('Image upload response status:', imageResponse.status);
          if (!imageResponse.ok) {
            const errorText = await imageResponse.text();
            console.error('Failed to upload image:', imageResponse.statusText, errorText);
          } else {
            console.log('Image uploaded successfully');
          }
        }
        
        await loadObjects();
        setIsModalOpen(false);
      } else {
        const errorText = await response.text();
        console.error('Failed to save object:', response.statusText, errorText);
      }
    } catch (error) {
      console.error('Error saving object:', error);
    }
  };

  const handleStatusChange = async (id: string, newStatus: string) => {
    setEditingStatusId(id);
    setEditingStatusValue(newStatus);
  };

  const handleApplyStatus = async () => {
    if (editingStatusId && editingStatusValue) {
      try {
        const token = localStorage.getItem('token');
        console.log('Saving status for object:', editingStatusId, 'new value:', editingStatusValue);
        const response = await fetch(`http://localhost:8090/api/v1/objects/${editingStatusId}/status`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
          body: JSON.stringify({ status: editingStatusValue }),
        });
        console.log('Status update response:', response.status);
        if (response.ok) {
          console.log('Status saved successfully, reloading objects');
          await loadObjects();
          setEditingStatusId(null);
          setEditingStatusValue('');
        } else {
          const errorText = await response.text();
          console.error('Failed to update status:', response.statusText, errorText);
        }
      } catch (error) {
        console.error('Error updating status:', error);
      }
    } else {
      setEditingStatusId(null);
      setEditingStatusValue('');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8090/api/v1/objects/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      if (response.ok) {
        await loadObjects();
      }
    } catch (error) {
      console.error('Error deleting object:', error);
    }
  };

  const handleAddObject = () => {
    setEditingObject(null);
    setIsModalOpen(true);
  };

  const handleEditObject = (obj: Object) => {
    setEditingObject(obj);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingObject(null);
  };

  return (
    <div>
      {isAdmin && editMode && (
        <div className="mb-6">
          <button 
            onClick={handleAddObject}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors"
          >
            + Добавить объект
          </button>
        </div>
      )}
      
      <div className="grid grid-cols-1 gap-6">
        {objects.map((obj) => (
          <div 
            key={obj.id}
            className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-200"
          >
            <div className="grid grid-cols-3 gap-6 p-6">
              <div className="col-span-1">
                {imageUrls[obj.id] ? (
                  <img src={imageUrls[obj.id]} alt={obj.name} className="w-full h-auto object-cover rounded-lg" style={{ minHeight: '300px' }} />
                ) : obj.imageUniqueName ? (
                  <div className="w-full h-80 bg-gray-200 flex items-center justify-center rounded-lg">
                    <span className="text-gray-400">Загрузка...</span>
                  </div>
                ) : (
                  <div className="w-full h-80 bg-gray-200 flex items-center justify-center rounded-lg">
                    <span className="text-gray-400">Фото объекта</span>
                  </div>
                )}
              </div>
              <div className="col-span-2 flex flex-col justify-between">
                <div>
                  <div className="mb-4">
                    <h3 className="text-3xl font-semibold text-gray-900 mb-2">{obj.name}</h3>
                    <div className="text-lg text-gray-600">{obj.address}</div>
                    <div className="text-lg text-gray-600 mt-2">{formatWorkType(obj.workType)}</div>
                  </div>
                  
                  <div className="mb-6">
                    <span className="text-gray-600 text-lg">Текущее состояние:</span>
                    {editMode ? (
                      <div className="flex items-center gap-2 mt-2">
                        <input 
                          type="text" 
                          value={editingStatusId === obj.id ? editingStatusValue : obj.status}
                          maxLength={100}
                          onChange={(e) => {
                            handleStatusChange(obj.id, e.target.value);
                          }}
                          className="border border-gray-300 rounded px-4 py-3 w-full text-lg"
                        />
                        {editingStatusId === obj.id && (
                          <button 
                            onClick={handleApplyStatus}
                            className="bg-green-600 hover:bg-green-700 text-white px-4 py-3 rounded text-lg whitespace-nowrap"
                          >
                            Применить
                          </button>
                        )}
                      </div>
                    ) : (
                      <span className="ml-3 font-medium text-gray-800 text-lg">{obj.status}</span>
                    )}
                  </div>

                  {(obj.customer || obj.responsibleEmployee) && (
                    <div className="bg-gray-50 rounded-lg p-4 mb-6">
                      {obj.customer && (
                        <div className="mb-3">
                          <span className="text-gray-600 text-lg">Заказчик:</span>
                          <span className="ml-3 text-gray-800 font-medium text-lg">{obj.customer.name}</span>
                        </div>
                      )}
                      {obj.responsibleEmployee && (
                        <div>
                          <span className="text-gray-600 text-lg">Ответственный:</span>
                          <div className="ml-3">
                            <div className="text-gray-800 font-medium text-lg">{obj.responsibleEmployee.fullName}</div>
                            {obj.responsibleEmployee.position && (
                              <div className="text-gray-600 text-lg">{obj.responsibleEmployee.position}</div>
                            )}
                            {obj.responsibleEmployee.phoneNumber && (
                              <div className="text-gray-600 text-lg">{obj.responsibleEmployee.phoneNumber}</div>
                            )}
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                </div>

                {editMode && isAdmin && (
                  <div className="flex gap-3">
                    <button 
                      onClick={() => handleEditObject(obj)}
                      className="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-4 py-3 rounded text-lg transition-colors"
                    >
                      Редактировать
                    </button>
                    <button 
                      onClick={() => handleDelete(obj.id)}
                      className="flex-1 bg-red-600 hover:bg-red-700 text-white px-4 py-3 rounded text-lg transition-colors"
                    >
                      Удалить
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
      
      <ObjectModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        onSave={handleSaveObject}
        object={editingObject}
      />
    </div>
  );
}
