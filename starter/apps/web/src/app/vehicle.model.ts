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

export interface VehicleSearchCriteria {
  query?: string;
  startDate?: string;
  endDate?: string;
  type?: string;
}

export interface VehicleSearchResult {
  records: Vehicle[];
  totalMatches: number;
  truncated: boolean;
}
