import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class StorageService {
  private storage: Storage = localStorage;

  public getItem(key: string): string | null {
    return this.storage.getItem(key);
  }

  public setItem(key: string, value: string): void {
    this.storage.setItem(key, value);
  }

  public removeItem(key: string): void {
    this.storage.removeItem(key);
  }
}