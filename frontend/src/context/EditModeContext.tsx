import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';

interface EditModeContextType {
  editMode: boolean;
  setEditMode: (editMode: boolean) => void;
}

const EditModeContext = createContext<EditModeContextType | undefined>(undefined);

export function EditModeProvider({ children }: { children: ReactNode }) {
  const [editMode, setEditMode] = useState(false);

  useEffect(() => {
    const savedMode = localStorage.getItem('editMode');
    if (savedMode === 'true') {
      setEditMode(true);
    }
  }, []);

  useEffect(() => {
    localStorage.setItem('editMode', editMode.toString());
  }, [editMode]);

  return (
    <EditModeContext.Provider value={{ editMode, setEditMode }}>
      {children}
    </EditModeContext.Provider>
  );
}

export function useEditMode() {
  const context = useContext(EditModeContext);
  if (context === undefined) {
    throw new Error('useEditMode must be used within an EditModeProvider');
  }
  return context;
}
