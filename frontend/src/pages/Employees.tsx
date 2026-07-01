import { useState, useEffect } from 'react';
import EmployeeModal from '@/components/EmployeeModal';

interface Employee {
  id: number;
  phoneNumber: string;
  fullName: string;
  position: string;
  customerId: number;
  customerName?: string;
}

export default function Employees() {
  const [employeeType, setEmployeeType] = useState<'company' | 'customers'>('company');
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [companyEmployees, setCompanyEmployees] = useState<Employee[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState<Employee | null>(null);

  useEffect(() => {
    loadEmployees();
  }, []);

  const loadEmployees = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8090/api/v1/employees', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      if (response.ok) {
        const data = await response.json();
        console.log('Loaded employees:', data);
        
        const company = data.find((e: Employee) => e.customerName === 'ЗАО "БЕЛСПЕЦЭНЕРГО"');
        const companyEmps = company ? data.filter((e: Employee) => e.customerId === company.customerId) : [];
        const customerEmps = data.filter((e: Employee) => e.customerName !== 'ЗАО "БЕЛСПЕЦЭНЕРГО"');
        
        setCompanyEmployees(companyEmps);
        setEmployees(customerEmps);
      }
    } catch (error) {
      console.error('Error loading employees:', error);
    }
  };

  const handleAddEmployee = () => {
    setEditingEmployee(null);
    setIsModalOpen(true);
  };

  const handleEditEmployee = (employee: Employee) => {
    setEditingEmployee(employee);
    setIsModalOpen(true);
  };

  const handleDeleteEmployee = async (id: number) => {
    if (!confirm('Удалить сотрудника?')) return;
    
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8090/api/v1/employees/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
      if (response.ok) {
        loadEmployees();
      }
    } catch (error) {
      console.error('Error deleting employee:', error);
    }
  };

  const handleSaveEmployee = async (data: any) => {
    try {
      const token = localStorage.getItem('token');
      const url = editingEmployee 
        ? `http://localhost:8090/api/v1/employees/${editingEmployee.id}`
        : 'http://localhost:8090/api/v1/employees';
      const method = editingEmployee ? 'PUT' : 'POST';
      
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(data),
      });
      
      if (response.ok) {
        setIsModalOpen(false);
        loadEmployees();
      }
    } catch (error) {
      console.error('Error saving employee:', error);
    }
  };

  const currentEmployees = employeeType === 'company' ? companyEmployees : employees;
  const isAddingCompanyEmployee = employeeType === 'company' && !editingEmployee;

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Сотрудники</h1>
          <div className="flex space-x-4">
            <button
              onClick={handleAddEmployee}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              Добавить сотрудника
            </button>
            <button
              onClick={() => setEmployeeType('company')}
              className={`px-4 py-2 rounded-lg transition-colors ${
                employeeType === 'company'
                  ? 'bg-blue-600 text-white'
                  : 'bg-white text-gray-700 hover:bg-gray-100'
              }`}
            >
              Сотрудники БЕЛСПЕЦЭНЕРГО
            </button>
            <button
              onClick={() => setEmployeeType('customers')}
              className={`px-4 py-2 rounded-lg transition-colors ${
                employeeType === 'customers'
                  ? 'bg-blue-600 text-white'
                  : 'bg-white text-gray-700 hover:bg-gray-100'
              }`}
            >
              Сотрудники предприятий-заказчиков
            </button>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-md overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  ФИО
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Телефон
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Должность
                </th>
                {employeeType === 'customers' && (
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Предприятие
                  </th>
                )}
                {employeeType === 'company' && (
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Разработанные ППР
                  </th>
                )}
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Действия
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {currentEmployees.map((employee) => (
                <tr key={employee.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {employee.fullName}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {employee.phoneNumber}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {employee.position}
                  </td>
                  {employeeType === 'customers' && (
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {employee.customerName}
                    </td>
                  )}
                  {employeeType === 'company' && (
                    <td className="px-6 py-4 text-sm text-gray-500">
                      -
                    </td>
                  )}
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button
                      onClick={() => handleEditEmployee(employee)}
                      className="text-blue-600 hover:text-blue-900 mr-4"
                    >
                      Изменить
                    </button>
                    <button
                      onClick={() => handleDeleteEmployee(employee.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      Удалить
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {currentEmployees.length === 0 && (
            <div className="text-center py-8 text-gray-500">
              Нет сотрудников
            </div>
          )}
        </div>
      </div>
      <EmployeeModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveEmployee}
        employee={editingEmployee}
        isCompanyEmployee={isAddingCompanyEmployee}
      />
    </div>
  );
}
