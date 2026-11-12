import {Component} from '@angular/core';

@Component({
  selector: 'app-access-restricted',
  template: `
    <div class="page">
      <div class="card-x" style="max-width:520px;margin:48px auto">
        <div class="empty-state" style="padding:48px 24px">
          <i class="bi bi-shield-lock" style="font-size:2.4rem;color:#a5b4fc"></i>
          <strong style="font-size:1.1rem">Administrator access required</strong>
          <small>Your account doesn't have the <span class="mono">ADMIN</span> role needed for this page.</small>
          <div class="mt-4"><a routerLink="/dashboard" class="btn btn-soft">Back to dashboard</a></div>
        </div>
      </div>
    </div>`
})
export class AccessRestrictedComponent {
}
