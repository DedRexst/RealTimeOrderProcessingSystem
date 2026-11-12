import {Component, EventEmitter, Output} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {BsModalRef} from 'ngx-bootstrap/modal';
import {finalize} from 'rxjs/operators';
import {ClientService} from '../../../services/client.service';
import {ClientCredentials, SCOPE_PATTERN, SUGGESTED_SCOPES} from '../../../services/models/client-models';
import {parseApiError} from '../../../utils/error-util';

const MAX_SCOPES = 32;

@Component({
  selector: 'app-register-client-modal',
  templateUrl: './register-client-modal.component.html'
})
export class RegisterClientModalComponent {
  @Output() registered = new EventEmitter<ClientCredentials>();

  readonly suggested = SUGGESTED_SCOPES;
  scopes: string[] = ['orders:write'];
  scopeInput = '';
  scopeError: string | null = null;
  submitting = false;
  error: string | null = null;
  fieldErrors: Record<string, string> = {};

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', Validators.maxLength(500)],
    contactEmail: ['', Validators.email]
  });

  constructor(public modalRef: BsModalRef, private fb: FormBuilder, private clients: ClientService) {
  }

  addScope(raw: string = this.scopeInput): void {
    const s = raw.trim().toLowerCase();
    this.scopeError = null;
    if (!s) {
      return;
    }
    if (!SCOPE_PATTERN.test(s)) {
      this.scopeError = 'Lowercase, no spaces, 2–64 chars, e.g. orders:write';
      return;
    }
    if (this.scopes.length >= MAX_SCOPES) {
      this.scopeError = `At most ${MAX_SCOPES} scopes.`;
      return;
    }
    if (!this.scopes.includes(s)) {
      this.scopes = [...this.scopes, s];
    }
    this.scopeInput = '';
  }

  onScopeKey(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ',' || event.key === ' ') {
      event.preventDefault();
      this.addScope();
    } else if (event.key === 'Backspace' && !this.scopeInput && this.scopes.length) {
      this.scopes = this.scopes.slice(0, -1);
    }
  }

  removeScope(s: string): void {
    this.scopes = this.scopes.filter(x => x !== s);
  }

  invalid(name: 'name' | 'description' | 'contactEmail'): boolean {
    const c = this.form.controls[name];
    return (c.invalid && (c.dirty || c.touched)) || !!this.fieldErrors[name];
  }

  submit(): void {
    if (this.scopeInput) {
      this.addScope();
    }
    if (this.form.invalid || !this.scopes.length) {
      this.form.markAllAsTouched();
      if (!this.scopes.length) {
        this.scopeError = 'Add at least one scope.';
      }
      return;
    }
    const v = this.form.getRawValue();
    this.submitting = true;
    this.error = null;
    this.fieldErrors = {};
    this.clients.register({
      name: v.name.trim(),
      description: v.description.trim() || undefined,
      contactEmail: v.contactEmail.trim() || undefined,
      scopes: this.scopes
    }).pipe(finalize(() => (this.submitting = false))).subscribe({
      next: creds => {
        this.modalRef.hide();
        this.registered.emit(creds);
      },
      error: err => {
        const e = parseApiError(err);
        this.error = e.message;
        this.fieldErrors = e.fieldErrors;
      }
    });
  }
}
