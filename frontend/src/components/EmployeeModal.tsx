import { useState, useEffect } from 'react';

interface EmployeeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: any) => void;
  employee?: any;
  isCompanyEmployee?: boolean;
}

interface Customer {
  id: number;
  name: string;
}

export default function EmployeeModal({ isOpen, onClose, onSave, employee, isCompanyEmployee }: EmployeeModalProps) {
  const [formData, setFormData] = useState({
    phoneNumber: '',
    fullName: '',
    position: '',
    customerId: '',
  });

  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen) {
      loadCustomers();
      if (employee) {
        setFormData({
          phoneNumber: employee.phoneNumber || '',
          fullName: employee.fullName || '',
          position: employee.position || '',
          customerId: employee.customerId?.toString() || '',
        });
      } else {
        setFormData({
          phoneNumber: '',
          fullName: '',
          position: '',
          customerId: '',
        });
      }
    }
  }, [employee, isOpen]);

  const loadCustomers = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('/api/v1/customers', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      if (response.ok) {
        const data = await response.json();
        setCustomers(data);
      }
    } catch (error) {
      console.error('Error loading customers:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const dataToSave = { ...formData };
    
    if (isBelSpecEnergo) {
      const belSpecEnergo = customers.find(c => c.name === 'ЗАО "БЕЛСПЕЦЭНЕРГО"');
      if (belSpecEnergo) {
        dataToSave.customerId = belSpecEnergo.id.toString();
      }
    }
    
    onSave(dataToSave);
  };

  const selectedCustomer = customers.find(c => c.id.toString() === formData.customerId);
  const isBelSpecEnergo = selectedCustomer?.name === 'ЗАО "БЕЛСПЕЦЭНЕРГО"' || isCompanyEmployee;

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
        <div className="p-6">
          <h2 className="text-xl font-semibold mb-4">
            {employee ? 'Редактировать сотрудника' : 'Добавить сотрудника'}
          </h2>
          <form onSubmit={handleSubmit}>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Телефон</label>
                <input
                  type="text"
                  value={formData.phoneNumber}
                  onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">ФИО</label>
                <input
                  type="text"
                  value={formData.fullName}
                  onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Должность</label>
                <input
                  type="text"
                  value={formData.position}
                  onChange={(e) => setFormData({ ...formData, position: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  required
                />
              </div>
              {!isBelSpecEnergo && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Предприятие</label>
                <select
                  value={formData.customerId}
                  onChange={(e) => setFormData({ ...formData, customerId: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  disabled={loading}
                  required
                >
                  <option value="">Выберите предприятие</option>
                  {customers.filter(c => !c.name.includes('БЕЛСПЕЦЭНЕРГО')).map((customer) => (
                    <option key={customer.id} value={customer.id}>
                      {customer.name}
                    </option>
                  ))}
                </select>
              </div>
              )}
            </div>
            <div className="flex justify-end space-x-3 mt-6">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
              >
                Отмена
              </button>
              <button
                type="submit"
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                Сохранить
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
