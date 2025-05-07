import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface Location {
  id: string;
  nombre: string;
  departamento: {
    id: string;
    nombre: string;
  };
  provincia: {
    id: string;
    nombre: string;
  };
}

export interface LocationResponse {
  cantidad: number;
  inicio: number;
  localidades: Location[];
  parametros: any;
  total: number;
}

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private apiUrl = 'https://apis.datos.gob.ar/georef/api';

  constructor(private http: HttpClient) { }

  getLocations(provincia: string, max: number = 514): Observable<Location[]> {
    return this.http.get<LocationResponse>(`${this.apiUrl}/localidades`, {
      params: {
        provincia: provincia,
        max: max.toString()
      }
    }).pipe(
      map(response => response.localidades)
    );
  }

  searchLocations(provincia: string, nombre: string): Observable<Location[]> {
    return this.http.get<LocationResponse>(`${this.apiUrl}/localidades`, {
      params: {
        provincia: provincia,
        nombre: nombre,
        max: '15',
        aplanar: 'true',
        exacto: 'false'
      }
    }).pipe(
      map(response => response.localidades)
    );
  }
} 