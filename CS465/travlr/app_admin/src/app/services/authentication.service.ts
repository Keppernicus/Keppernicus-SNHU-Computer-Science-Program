import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { StorageService } from './storage.service';
import { User } from '../models/user';
import { AuthResponse } from '../models/authresponse';

@Injectable({ providedIn: 'root' })
export class AuthenticationService {
  private apiBaseUrl = 'http://localhost:3000/api';

  constructor(
    private http: HttpClient,
    private storage: StorageService
  ) { }

  public getToken(): string | null {
    return this.storage.getItem('travlr-token');
  }

  public saveToken(token: string): void {
    this.storage.setItem('travlr-token', token);
  }

  public logout(): void {
    this.storage.removeItem('travlr-token');
  }

  public isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp > Date.now() / 1000;
  }

  public getCurrentUser(): User | null {
    if (!this.isLoggedIn()) return null;
    const token = this.getToken()!;
    const { email, name } = JSON.parse(atob(token.split('.')[1]));
    return { email, name } as User;
  }

  public login(email: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiBaseUrl}/login`, { email, password })
      .pipe(tap((res) => this.saveToken(res.token)));
  }

  public register(name: string, email: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiBaseUrl}/register`, { name, email, password })
      .pipe(tap((res) => this.saveToken(res.token)));
  }
}