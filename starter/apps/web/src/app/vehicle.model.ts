export interface Vehicle {
  uuid: string;
  status: 'REGISTERED' | 'DEREGISTERED';
  startDate: string | null;
  endDate: string | null;
  time: string | null;
  userUuid: string;
  brand: string;
  type: string;
  category: 'personal' | 'commercial';
}
