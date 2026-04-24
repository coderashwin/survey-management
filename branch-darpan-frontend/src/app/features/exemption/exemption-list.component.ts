import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { WorkflowRequest } from '../../core/models/app.models';
import { ExemptionService } from '../../core/services/exemption.service';
import { ListingComponent } from '../../shared/components/listing/listing.component';

@Component({
  selector: 'app-exemption-list',
  standalone: true,
  imports: [ListingComponent],
  template: `
    <section class="stack">
      <div class="panel-header">
        <div>
          <div class="eyebrow">Exemption Workflow</div>
          <h2 class="section-title">Survey-specific branch exemptions</h2>
        </div>
        <button class="btn btn-primary" type="button" (click)="router.navigate(['/app/exemption/create'])">Create Exemption</button>
      </div>
      <app-listing [columns]="columns" [data]="items" (rowClicked)="open($event)" />
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExemptionListComponent {
  readonly router = inject(Router);
  private readonly exemptionService = inject(ExemptionService);

  items: WorkflowRequest[] = [];
  columns = [
    { key: 'branchCode', label: 'Branch' },
    { key: 'status', label: 'Status', kind: 'status' as const },
    { key: 'createdAt', label: 'Created', kind: 'date' as const },
  ];

  constructor() {
    this.exemptionService.list().subscribe((items) => (this.items = items));
  }

  open(row: any): void {
    this.router.navigate(['/app/exemption', row['id']]);
  }
}
