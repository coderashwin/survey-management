import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { HistoryItem, PagedResponse } from '../../core/models/app.models';
import { HistoryService } from '../../core/services/history.service';
import { ListingComponent } from '../../shared/components/listing/listing.component';

const HISTORY_ROUTE_MAP: Record<string, (item: HistoryItem) => { commands: unknown[]; queryParams?: Record<string, unknown> }> = {
  USER_REQUEST: (item) => ({ commands: ['/app/users'] }),
  SURVEY_ATTEMPT: (item) => ({ commands: ['/app/survey-approval', item.referenceId] }),
  SURVEY_DRAFT: (item) => ({ commands: ['/app/surveys'] }),
  REVERSAL_REQUEST: (item) => ({ commands: ['/app/reversal', item.referenceId] }),
  EXEMPTION_REQUEST: (item) => ({ commands: ['/app/exemption', item.referenceId] }),
  SURVEY_CONFIGURATION: () => ({ commands: ['/app/questionnaire-config'] }),
};

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [ListingComponent],
  template: `
    <section class="stack">
      <div class="panel-header">
        <div>
          <div class="eyebrow">History</div>
          <h2 class="section-title">Audit trail with direct navigation hooks</h2>
        </div>
      </div>
      <app-listing [columns]="columns" [data]="page.content" (rowClicked)="open($event)" />
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HistoryComponent {
  private readonly historyService = inject(HistoryService);
  private readonly router = inject(Router);

  page: PagedResponse<HistoryItem> = { content: [], totalElements: 0, totalPages: 0, currentPage: 0 };
  columns = [
    { key: 'requestType', label: 'Type' },
    { key: 'actorPfid', label: 'Creator PFID' },
    { key: 'status', label: 'Status', kind: 'status' as const },
    { key: 'createdAt', label: 'Date', kind: 'date' as const },
  ];

  constructor() {
    this.historyService.list({ page: 0, size: 20 }).subscribe((page) => (this.page = page));
  }

  open(row: any): void {
    const item = row as unknown as HistoryItem;
    const target = HISTORY_ROUTE_MAP[item.requestType];
    if (!target) {
      return;
    }
    const route = target(item);
    this.router.navigate(route.commands, { queryParams: route.queryParams });
  }
}
