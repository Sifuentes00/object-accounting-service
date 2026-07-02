import { useState, useEffect } from 'react';

interface ContractModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: any, file?: File | null) => void;
  contract?: any;
  objectId: string;
}

export default function ContractModal({ isOpen, onClose, onSave, contract, objectId }: ContractModalProps) {
  const [formData, setFormData] = useState({
    number: '',
    conclusionDate: '',
    endDate: '',
    file: null as File | null,
  });

  useEffect(() => {
    if (contract) {
      setFormData({
        number: contract.number,
        conclusionDate: contract.conclusionDate,
        endDate: contract.endDate,
        file: null,
      });
    } else {
      setFormData({
        number: '',
        conclusionDate: '',
        endDate: '',
        file: null,
      });
    }
  }, [contract]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const dataToSave = {
      number: formData.number,
      conclusionDate: formData.conclusionDate,
      endDate: formData.endDate,
      objectId: parseInt(objectId),
      fileUniqueName: contract ? contract.fileUniqueName : `temp-${Date.now()}`,
    };
    onSave(dataToSave, formData.file);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
        <div className="p-6">
          <h2 className="text-xl font-semibold mb-4">
            {contract ? 'Редактировать договор' : 'Добавить договор'}
          </h2>
          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label className="block text-gray-700 font-medium mb-2">Номер договора</label>
              <input
                type="text"
                value={formData.number}
                onChange={(e) => setFormData({ ...formData, number: e.target.value })}
                className="w-full border border-gray-300 rounded px-3 py-2"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700 font-medium mb-2">Дата заключения</label>
              <input
                type="date"
                value={formData.conclusionDate}
                onChange={(e) => setFormData({ ...formData, conclusionDate: e.target.value })}
                className="w-full border border-gray-300 rounded px-3 py-2"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700 font-medium mb-2">Дата окончания</label>
              <input
                type="date"
                value={formData.endDate}
                onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                className="w-full border border-gray-300 rounded px-3 py-2"
                required
              />
            </div>
            <div className="mb-4">
              <label className="block text-gray-700 font-medium mb-2">Файл договора (PDF)</label>
              <input
                type="file"
                accept="application/pdf"
                onChange={(e) => setFormData({ ...formData, file: e.target.files?.[0] || null })}
                className="w-full border border-gray-300 rounded px-3 py-2"
                required={!contract}
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
