import {Component, EventEmitter, Output} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {BsModalRef} from 'ngx-bootstrap/modal';
import {finalize} from 'rxjs/operators';
import {CatalogService} from '../../../services/catalog.service';
import {ProductRef} from '../../../services/models/catalog-models';
import {parseApiError} from '../../../utils/error-util';

@Component({
  selector: 'app-add-product-modal',
  templateUrl: './add-product-modal.component.html'
})
export class AddProductModalComponent {
  @Output() created = new EventEmitter<ProductRef>();

  submitting = false;
  error: string | null = null;
  fieldErrors: Record<string, string> = {};

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
    description: ['', Validators.maxLength(255)],
    initialStock: [0, [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)]]
  });

  constructor(public modalRef: BsModalRef, private fb: FormBuilder, private catalog: CatalogService) {
  }

  invalid(name: 'name' | 'description' | 'initialStock'): boolean {
    const c = this.form.controls[name];
    return (c.invalid && (c.dirty || c.touched)) || !!this.fieldErrors[name];
  }

  submit(): void {
    if (this.form.invalid || !this.form.getRawValue().name.trim()) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.submitting = true;
    this.error = null;
    this.fieldErrors = {};
    this.catalog.createProduct({
      name: v.name.trim(),
      description: v.description.trim() || undefined,
      initialStock: Number(v.initialStock)
    }).pipe(finalize(() => (this.submitting = false))).subscribe({
      next: product => {
        this.modalRef.hide();
        this.created.emit(product);
      },
      error: err => {
        const e = parseApiError(err);
        this.error = e.message;
        this.fieldErrors = e.fieldErrors;
      }
    });
  }
}
