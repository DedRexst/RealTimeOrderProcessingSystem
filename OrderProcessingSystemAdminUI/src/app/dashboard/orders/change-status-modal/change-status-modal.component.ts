import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {BsModalRef} from 'ngx-bootstrap/modal';
import {finalize} from 'rxjs/operators';
import {OrderService} from '../../../services/order.service';
import {AuthService} from '../../../services/auth.service';
import {OrderResponse, OrderStatus} from '../../../services/models/order-models';
import {nextStatuses, statusMeta} from '../../../utils/constants/order-status-constants';
import {parseApiError} from '../../../utils/error-util';

const REASON_PRESETS: Partial<Record<OrderStatus, string[]>> = {
  SUCCESSFUL: ['Delivered to customer', 'Payment captured'],
  FAILED: ['Payment declined', 'Out of stock', 'Customer cancelled', 'Fraud suspected'],
  RELEASED: ['Customer cancelled', 'Reservation expired'],
  RESERVED: ['Manually reserved']
};

@Component({
  selector: 'app-change-status-modal',
  templateUrl: './change-status-modal.component.html'
})
export class ChangeStatusModalComponent implements OnInit {
  @Input({required: true}) order!: OrderResponse;
  @Input() target: OrderStatus | null = null;
  @Output() changed = new EventEmitter<OrderResponse>();

  readonly statusMeta = statusMeta;
  options: OrderStatus[] = [];
  presets: string[] = [];
  submitting = false;
  error: string | null = null;

  readonly form = this.fb.nonNullable.group({
    status: this.fb.control<OrderStatus | null>(null, Validators.required),
    reason: ['', Validators.maxLength(500)]
  });

  constructor(public modalRef: BsModalRef,
              private fb: FormBuilder,
              private orders: OrderService,
              public auth: AuthService) {
  }

  ngOnInit(): void {
    this.options = nextStatuses(this.order.status);
    this.select(this.target && this.options.includes(this.target) ? this.target : this.options[0] ?? null);
  }

  select(status: OrderStatus | null): void {
    this.form.controls.status.setValue(status);
    this.presets = status ? REASON_PRESETS[status] ?? [] : [];
  }

  usePreset(p: string): void {
    this.form.controls.reason.setValue(p);
  }

  get reasonLength(): number {
    return this.form.controls.reason.value.length;
  }

  submit(): void {
    const {status, reason} = this.form.getRawValue();
    if (!status || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    this.error = null;
    this.orders.updateStatus(this.order.id, {status, reason: reason.trim() || undefined})
      .pipe(finalize(() => (this.submitting = false)))
      .subscribe({
        next: updated => {
          this.changed.emit(updated);
          this.modalRef.hide();
        },
        error: err => {
          const e = parseApiError(err);
          this.error = e.status === 409 ? `${e.message}. The order may have changed. Refresh and try again.` : e.message;
        }
      });
  }
}
