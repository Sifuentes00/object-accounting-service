import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';

interface AuthContextType {
  isAuthenticated: boolean;
  fullName: string | null;
  email: string | null;
  role: string | null;
  token: string | null;
  login: (fullName: string, email: string, role: string, token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [fullName, setFullName] = useState<string | null>(null);
  const [email, setEmail] = useState<string | null>(null);
  const [role, setRole] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    const auth = localStorage.getItem('auth');
    if (auth) {
      const { isAuthenticated, fullName, email, role, token } = JSON.parse(auth);
      setIsAuthenticated(isAuthenticated);
      setFullName(fullName);
      setEmail(email);
      setRole(role);
      setToken(token);
    }
  }, []);

  const login = (fullName: string, email: string, role: string, token: string) => {
    setIsAuthenticated(true);
    setFullName(fullName);
    setEmail(email);
    setRole(role);
    setToken(token);
    localStorage.setItem('auth', JSON.stringify({ isAuthenticated: true, fullName, email, role, token }));
    localStorage.setItem('token', token);
  };

  const logout = () => {
    setIsAuthenticated(false);
    setFullName(null);
    setEmail(null);
    setRole(null);
    setToken(null);
    localStorage.removeItem('auth');
    localStorage.removeItem('token');
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, fullName, email, role, token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
