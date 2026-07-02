import React, { createContext, useContext, useState } from 'react';
import type { ReactNode } from 'react';

interface ErrorContextType {
  showError: (message: string) => void;
  clearError: () => void;
  error: string | null;
}

const ErrorContext = createContext<ErrorContextType | undefined>(undefined);

export const ErrorProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [error, setError] = useState<string | null>(null);

  const showError = (message: string) => {
    setError(message);
  };

  const clearError = () => {
    setError(null);
  };

  return (
    <ErrorContext.Provider value={{ showError, clearError, error }}>
      {children}
    </ErrorContext.Provider>
  );
};

export const useError = () => {
  const context = useContext(ErrorContext);
  if (context === undefined) {
    throw new Error('useError must be used within an ErrorProvider');
  }
  return context;
};
