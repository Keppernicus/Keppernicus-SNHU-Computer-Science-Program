import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthenticationService } from '../services/authentication.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  public formError: string = '';
  public loginForm!: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authenticationService: AuthenticationService
  ) {
    this.loginForm = this.formBuilder.group({
      email: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  public onLoginSubmit(): void {
    this.formError = '';
    if (this.loginForm.invalid) {
      this.formError = 'All fields are required, please try again';
      return;
    }
    const { email, password } = this.loginForm.value;
    this.authenticationService.login(email, password).subscribe({
      next: () => this.router.navigate(['']),
      error: (err) => {
        this.formError = err.error?.message || 'Login failed, please try again';
      }
    });
  }
}