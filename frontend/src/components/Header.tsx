export default function Header() {
  return (
    <header className="bg-blue-800 shadow-lg relative">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center h-24 relative">
          <div className="flex items-center space-x-4 -ml-8">
            <img 
              src="/logo.jpeg" 
              alt="Логотип" 
              className="w-12 h-12 rounded-xl"
            />
            <div>
              <div className="text-xl font-bold text-white">ЗАО БЕЛСПЕЦЭНЕРГО</div>
            </div>
          </div>
          <div className="absolute left-1/2 transform -translate-x-1/2">
            <div className="text-3xl font-bold text-white">Система учета объектов</div>
          </div>
        </div>
      </div>
    </header>
  );
}
