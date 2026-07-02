import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useEditMode } from '../context/EditModeContext';

interface HeaderProps {
  hideProfile?: boolean;
}

export default function Header({ hideProfile = false }: HeaderProps) {
  const { fullName, email, role, logout, isAuthenticated } = useAuth();
  const { editMode, setEditMode } = useEditMode();
  const navigate = useNavigate();
  const location = useLocation();
  const [showProfile, setShowProfile] = useState(false);

  const isAdmin = role === 'ADMIN';
  const isCustomersPage = location.pathname.startsWith('/main/customers');
  const isEmployeesPage = location.pathname.startsWith('/main/employees');
  const showEditModeToggle = !(isCustomersPage || isEmployeesPage);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="bg-blue-800 shadow-lg relative">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-24">
          <div className="flex items-center space-x-4 -ml-24">
            <div 
              className="flex items-center space-x-4 cursor-pointer"
              onClick={() => isAuthenticated && navigate('/main')}
            >
              <img 
                src="/logo.jpeg" 
                alt="Логотип" 
                className="w-12 h-12 rounded-xl"
              />
              <div>
                <div className="text-xl font-bold text-white">ЗАО БЕЛСПЕЦЭНЕРГО</div>
              </div>
            </div>
          </div>
          <div className="absolute left-1/2 transform -translate-x-1/2">
            <div className="text-3xl font-bold text-white">Система учета объектов</div>
          </div>
          
          <div className="flex items-center space-x-4 ml-auto -mr-24">
            {isAuthenticated && !hideProfile && (
              <div 
                className="relative group"
                onMouseEnter={() => setShowProfile(true)}
                onMouseLeave={() => setShowProfile(false)}
              >
                <div className="flex items-center space-x-3 bg-blue-700 hover:bg-blue-600 px-4 py-2 rounded-lg cursor-pointer transition-all duration-200">
                  <div className="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center">
                    <span className="text-white font-semibold">
                      {fullName?.split(' ').map(n => n[0]).join('')}
                    </span>
                  </div>
                  <span className="text-white font-medium">{fullName}</span>
                </div>
                
                {showProfile && (
                  <div className="absolute right-0 top-full w-64 bg-white rounded-lg shadow-xl z-50 overflow-hidden">
                    <div className="p-4 border-b">
                      <div className="font-semibold text-gray-800">{fullName}</div>
                      <div className="text-sm text-gray-600">{email}</div>
                    </div>
                    
                    {isAdmin && (
                      <div className="border-b">
                        <button 
                          className="w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-100 transition-colors"
                          onClick={() => navigate('/main/employees')}
                        >
                          Просмотреть сотрудников
                        </button>
                        <button 
                          className="w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-100 transition-colors"
                          onClick={() => navigate('/main/customers')}
                        >
                          Просмотреть заказчиков
                        </button>
                        {showEditModeToggle && (
                          <button 
                            className="w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-100 transition-colors flex items-center justify-between"
                            onClick={() => setEditMode(!editMode)}
                          >
                            <span>Режим редактирования</span>
                            <div className="relative">
                              <div className={`w-11 h-6 rounded-full transition-colors duration-200 ${editMode ? 'bg-blue-500' : 'bg-gray-300'}`}></div>
                              <div className={`absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform duration-200 ${editMode ? 'translate-x-5' : 'translate-x-0.5'}`}></div>
                            </div>
                          </button>
                        )}
                      </div>
                    )}
                    
                    <button 
                      className="w-full text-left px-4 py-3 text-red-600 hover:bg-red-50 transition-colors font-medium"
                      onClick={handleLogout}
                    >
                      Выйти из профиля
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
