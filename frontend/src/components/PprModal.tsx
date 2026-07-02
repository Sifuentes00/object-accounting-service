import { useState, useEffect } from 'react';

interface PprModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: any, file?: File | null) => void;
  ppr?: any;
  objectId: string;
  employees: any[];
}

export default function PprModal({ isOpen, onClose, onSave, ppr, objectId, employees }: PprModalProps) {
  const [formData, setFormData] = useState({
    name: '',
    archiveNumber: '',
    number: '',
    employeeId: '',
    file: null as File | null,
  });

  console.log('PprModal employees:', employees);
  console.log('PprModal employees filtered:', employees.filter(e => e.customer?.name?.includes('БЕЛСПЕЦЭНЕРГО') || !e.customerId));

  useEffect(() => {
    if (ppr) {
      setFormData({
        name: ppr.name,
        archiveNumber: ppr.archiveNumber,
        number: ppr.number,
        employeeId: ppr.employee?.id || '',
        file: null,
      });
    } else {
      setFormData({
        name: '',
        archiveNumber: '',
        number: '',
        employeeId: '',
        file: null,
      });
    }
  }, [ppr]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const dataToSave = {
      name: formData.name,
      archiveNumber: formData.archiveNumber,
      number: formData.number,
      objectId: parseInt(objectId),
      employeeId: formData.employeeId ? parseInt(formData.employeeId) : null,
      fileUniqueName: ppr ? ppr.fileUniqueName : `temp-${Date.now()}`,
    };
    onSave(dataToSave, formData.file);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
        <div className="p-6">
          <h2 className="text-xl font-semibold mb-4">
            {ppr ? 'Редактировать ППР' : 'Добавить ППР'}
          </h2>
          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label className="block text-gray-700 font-medium mb-2">Название ППР</label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="w-full border border-gray-300 rounded px-3 py-2"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700 font-medium mb-2">Архивный номер</label>
              <input
                type="text"
                value={formData.archiveNumber}
                onChange={(e) => setFormData({ ...formData, archiveNumber: e.target.value })}
                className="w-full border border-gray-300 rounded px-3 py-2"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700 font-medium mb-2">Номер ППР</label>
              <input
                type="text"
                value={formData.number}
                onChange={(e) => setFormData({ ...formData, number: e.target.value })}
                className="w-full border border-gray-300 rounded px-3 py-2"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700 font-medium mb-2">Разработчик</label>
              <select
                value={formData.employeeId}
                onChange={(e) => setFormData({ ...formData, employeeId: e.target.value })}
                className="w-full border border-gray-300 rounded px-3 py-2"
              >
                <option value="">Не выбран</option>
                {employees.filter(e => e.customerName?.includes('БЕЛСПЕЦЭНЕРГО')).map((employee) => (
                  <option key={employee.id} value={employee.id}>
                    {employee.fullName}
                  </option>
                ))}
              </select>
            </div>
            <div className="mb-4">
              <label className="block text-gray-700 font-medium mb-2">Файл ППР (PDF)</label>
              <input
                type="file"
                accept="application/pdf"
                onChange={(e) => setFormData({ ...formData, file: e.target.files?.[0] || null })}
                className="w-full border border-gray-300 rounded px-3 py-2"
                required={!ppr}
              />
            </div>
            <div className="flex gap-3 mt-6">
              <button
                type="submit"
                className="flex-1 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
              >
                Сохранить
              </button>
              <button
                type="button"
                onClick={onClose}
                className="flex-1 bg-gray-300 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-400"
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
