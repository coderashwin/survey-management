import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { WorkflowRequest } from '../../core/models/app.models';
import { ReversalService } from '../../core/services/reversal.service';
import { ListingComponent } from '../../shared/components/listing/listing.component';

@Component({
  selector: 'app-reversal-list',
  standalone: true,
  imports: [ListingComponent],
  template: `
    <section class="stack">
      <div class="panel-header">
        <div>
          <div class="eyebrow">Reversal Workflow</div>
          <h2 class="section-title">Circle to CC approval chain</h2>
        </div>
        <button class="btn btn-primary" type="button" (click)="router.navigate(['/app/reversal/create'])">Create Reversal</button>
      </div>
      <app-listing [columns]="columns" [data]="items" (rowClicked)="open($event)" />
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReversalListComponent {
  readonly router = inject(Router);
  private readonly reversalService = inject(ReversalService);

  items: WorkflowRequest[] = [];
  columns = [
    { key: 'branchCode', label: 'Branch' },
    { key: 'status', label: 'Status', kind: 'status' as const },
    { key: 'createdAt', label: 'Created', kind: 'date' as const },
  ];

  constructor() {
    this.reversalService.list().subscribe((items) => (this.items = items));
  }

  open(row: any): void {
    this.router.navigate(['/app/reversal', row['id']]);
  }
}
