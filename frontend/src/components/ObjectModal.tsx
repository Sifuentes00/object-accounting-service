import { useState, useEffect } from 'react';

interface ObjectModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: any) => void;
  object?: any;
}

interface Customer {
  id: number;
  name: string;
}

interface Employee {
  id: number;
  phoneNumber: string;
  fullName: string;
  position: string;
  customerId: number | null;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export default function ObjectModal({ isOpen, onClose, onSave, object }: ObjectModalProps) {
  const [formData, setFormData] = useState({
    name: '',
    status: '',
    address: '',
    workType: '',
    customerId: '',
    responsibleEmployeeId: '',
    image: null as File | null,
  });

  const [customers, setCustomers] = useState<Customer[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loadingCustomers, setLoadingCustomers] = useState(false);
  const [loadingEmployees, setLoadingEmployees] = useState(false);
  const [employeeError, setEmployeeError] = useState('');
  const [currentImageUrl, setCurrentImageUrl] = useState<string>('');
  const [newImagePreview, setNewImagePreview] = useState<string>('');

  useEffect(() => {
    if (isOpen) {
      loadCustomers();
      if (object) {
        setFormData({
          name: object.name || '',
          status: object.status || '',
          address: object.address || '',
          workType: object.workType || '',
          customerId: object.customer?.id?.toString() || '',
          responsibleEmployeeId: object.responsibleEmployee?.id?.toString() || '',
          image: null,
        });
        if (object.customer?.id) {
          loadEmployees(object.customer.id.toString());
        }
        // Load current image preview
        if (object.imageUniqueName) {
          loadCurrentImage(object.id);
        } else {
          setCurrentImageUrl('');
        }
      } else {
        setFormData({
          name: '',
          status: '',
          address: '',
          workType: '',
          customerId: '',
          responsibleEmployeeId: '',
          image: null,
        });
        setEmployees([]);
        setCurrentImageUrl('');
      }
      setNewImagePreview('');
    }
  }, [object, isOpen]);

  const loadCurrentImage = async (objectId: number) => {
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
        setCurrentImageUrl(url);
      }
    } catch (error) {
      console.error('Error loading current image:', error);
    }
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    console.log('File selected:', file?.name);
    setFormData({ ...formData, image: file || null });
    
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        console.log('FileReader completed, setting preview, length:', (reader.result as string).length);
        setNewImagePreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    } else {
      console.log('No file selected, clearing preview');
      setNewImagePreview('');
    }
  };

  // Debug: log when newImagePreview changes
  useEffect(() => {
    console.log('newImagePreview changed:', newImagePreview ? 'has value' : 'empty');
  }, [newImagePreview]);

  const loadCustomers = async () => {
    setLoadingCustomers(true);
    try {
      const token = localStorage.getItem('token');
      console.log('Loading customers with token:', token ? `exists (${token.substring(0, 20)}...)` : 'missing');
      console.log('Full token from localStorage:', token);
      const response = await fetch('http://localhost:8090/api/v1/customers', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      console.log('Customers response status:', response.status);
      console.log('Customers response headers:', response.headers);
      const responseText = await response.text();
      console.log('Customers response body:', responseText);
      if (response.ok) {
        const data = JSON.parse(responseText);
        console.log('Customers data:', data);
        setCustomers(data);
      } else {
        console.error('Failed to load customers:', response.status, responseText);
      }
    } catch (error) {
      console.error('Error loading customers:', error);
    } finally {
      setLoadingCustomers(false);
    }
  };

  const loadEmployees = async (customerId: string | number) => {
    setLoadingEmployees(true);
    setEmployeeError('');
    try {
      console.log('Loading employees for customer:', customerId);
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8090/api/v1/employees/customer/${customerId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      console.log('Employees response status:', response.status);
      const responseText = await response.text();
      console.log('Employees response body:', responseText);
      if (response.ok) {
        const data = JSON.parse(responseText);
        console.log('Employees data:', data);
        setEmployees(data);
      } else {
        console.error('Failed to load employees:', response.status, responseText);
        setEmployeeError('Не удалось загрузить сотрудников');
      }
    } catch (error) {
      console.error('Error loading employees:', error);
      setEmployeeError('Ошибка при загрузке сотрудников');
    } finally {
      setLoadingEmployees(false);
    }
  };

  const handleCustomerChange = (customerId: string) => {
    setFormData({ ...formData, customerId, responsibleEmployeeId: '' });
    setEmployees([]);
    setEmployeeError('');
    if (customerId) {
      loadEmployees(customerId);
    }
  };

  const handleEmployeeChange = (employeeId: string) => {
    if (!formData.customerId) {
      setEmployeeError('Сначала выберите заказчика');
      return;
    }
    setFormData({ ...formData, responsibleEmployeeId: employeeId });
    setEmployeeError('');
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.responsibleEmployeeId && !formData.customerId) {
      setEmployeeError('Сначала выберите заказчика');
      return;
    }
    onSave(formData);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">
            {object ? 'Редактировать объект' : 'Добавить объект'}
          </h2>
          
          <form onSubmit={handleSubmit}>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Название</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Статус</label>
                <textarea
                  value={formData.status}
                  onChange={(e) => {
                    let value = e.target.value;
                    const lines = value.split('\n');
                    const processedLines = lines.map(line => {
                      if (line.length > 50) {
                        let result = '';
                        for (let i = 0; i < line.length; i += 50) {
                          result += line.slice(i, i + 50) + '\n';
                        }
                        return result.trim();
                      }
                      return line;
                    });
                    setFormData({ ...formData, status: processedLines.join('\n') });
                  }}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 resize-y"
                  rows={3}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Адрес</label>
                <input
                  type="text"
                  value={formData.address}
                  onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Тип работ</label>
                <select
                  value={formData.workType}
                  onChange={(e) => setFormData({ ...formData, workType: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                >
                  <option value="">Выберите тип работ</option>
                  <option value="DESIGN">Проектирование</option>
                  <option value="GEODESY">Геодезия</option>
                  <option value="CONSTRUCTION_INSTALLATION">Строительно-монтажные работы</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Заказчик</label>
                <select
                  value={formData.customerId}
                  onChange={(e) => handleCustomerChange(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  disabled={loadingCustomers}
                >
                  <option value="">Выберите заказчика</option>
                  {customers.filter(c => c.name !== 'ЗАО "БЕЛСПЕЦЭНЕРГО"').map((customer) => (
                    <option key={customer.id} value={customer.id}>
                      {customer.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Ответственный сотрудник</label>
                <select
                  value={formData.responsibleEmployeeId}
                  onChange={(e) => handleEmployeeChange(e.target.value)}
                  onClick={() => {
                    if (!formData.customerId) {
                      setEmployeeError('Сначала выберите заказчика');
                    }
                  }}
                  className={`w-full border rounded-lg px-3 py-2 ${employeeError ? 'border-red-500 bg-red-50' : 'border-gray-300'}`}
                  disabled={loadingEmployees || !formData.customerId}
                >
                  <option value="">Выберите сотрудника</option>
                  {employees.map((employee) => (
                    <option key={employee.id} value={employee.id}>
                      {employee.fullName}
                    </option>
                  ))}
                </select>
                {employeeError && (
                  <p className="text-red-600 text-sm mt-1 font-semibold">{employeeError}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Фото объекта</label>
                <input
                  key={newImagePreview || 'file-input'}
                  type="file"
                  accept="image/*"
                  onChange={handleImageChange}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                />
                {newImagePreview ? (
                  <div className="mt-2">
                    <p className="text-sm text-gray-600 mb-1">Новое фото:</p>
                    <img src={newImagePreview} alt="Preview" className="w-32 h-32 object-cover rounded-lg border border-gray-300" />
                  </div>
                ) : currentImageUrl ? (
                  <div className="mt-2">
                    <p className="text-sm text-gray-600 mb-1">Текущее фото:</p>
                    <img src={currentImageUrl} alt="Current" className="w-32 h-32 object-cover rounded-lg border border-gray-300" />
                  </div>
                ) : null}
              </div>
            </div>

            <div className="flex gap-3 mt-6">
              <button
                type="submit"
                className="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors"
              >
                {object ? 'Сохранить' : 'Добавить'}
              </button>
              <button
                type="button"
                onClick={onClose}
                className="flex-1 bg-gray-300 hover:bg-gray-400 text-gray-700 px-4 py-2 rounded-lg transition-colors"
              >
                Отмена
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
