import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Vehicle } from './vehicle.model';

@Injectable({ providedIn: 'root' })
export class VehicleService {
  private readonly baseUrl = 'http://localhost:8080/api/vehicles';

  constructor(private http: HttpClient) {}

  list(page: number, uuid?: string): Observable<Vehicle[]> {
    const lookup = uuid === undefined ? '' : `&uuid=${encodeURIComponent(uuid)}`;
    return this.http.get<Vehicle[]>(`${this.baseUrl}?page=${page}${lookup}`);
  }

  getOne(uuid: string): Observable<Vehicle> {
    return this.http.get<Vehicle>(`${this.baseUrl}/${uuid}`);
  }
}
