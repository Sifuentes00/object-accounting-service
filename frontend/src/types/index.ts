export interface Customer {
  id: number;
  name: string;
  legalAddress: string;
}

export interface Employee {
  id: number;
  fullName: string;
  phoneNumber: string;
  position: string;
  customerId: number;
}

export interface ObjectEntity {
  id: number;
  name: string;
  address: string;
  status: string;
  workType: string;
  customerId: number;
  responsibleEmployeeId: number;
}

export interface Contract {
  id: number;
  number: string;
  conclusionDate: string;
  endDate: string;
  fileUniqueName: string;
  objectId: number;
}

export interface Ppr {
  id: number;
  number: string;
  name: string;
  archiveNumber: string;
  fileUniqueName: string;
  objectId: number;
  employeeId: number;
}

export interface User {
  username: string;
  password: string;
}

export interface AuthResponse {
  access_token: string;
}
