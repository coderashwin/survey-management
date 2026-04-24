import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';

export interface ColumnDef {
  key: string;
  label: string;
  kind?: 'text' | 'date' | 'status';
}

export interface ActionDef {
  label: string;
  action: string;
  tone?: 'primary' | 'secondary' | 'danger';
}

@Component({
  selector: 'app-listing',
  standalone: true,
  imports: [CommonModule, DatePipe],
  template: `
    <div class="panel">
      <table class="data-table">
        <thead>
          <tr>
            @for (column of columns; track column.key) {
              <th>{{ column.label }}</th>
            }
            @if (actions.length) {
              <th>Actions</th>
            }
          </tr>
        </thead>
        <tbody>
          @for (row of data; track trackBy(row)) {
            <tr (click)="rowClicked.emit(row)">
              @for (column of columns; track column.key) {
                <td>
                  @if (column.kind === 'status') {
                    <span class="pill" [class.approved]="isApproved(row[column.key])" [class.pending]="isPending(row[column.key])" [class.rejected]="isRejected(row[column.key])">
                      {{ row[column.key] }}
                    </span>
                  } @else if (column.kind === 'date') {
                    {{ row[column.key] | date: 'medium' }}
                  } @else {
                    {{ row[column.key] }}
                  }
                </td>
              }
              @if (actions.length) {
                <td>
                  <div class="btn-row">
                    @for (action of actions; track action.action) {
                      <button
                        class="btn"
                        [class.btn-primary]="action.tone === 'primary'"
                        [class.btn-danger]="action.tone === 'danger'"
                        [class.btn-secondary]="!action.tone || action.tone === 'secondary'"
                        type="button"
                        (click)="emitAction($event, action.action, row)"
                      >
                        {{ action.label }}
                      </button>
                    }
                  </div>
                </td>
              }
            </tr>
          } @empty {
            <tr>
              <td [attr.colspan]="columns.length + (actions.length ? 1 : 0)">
                <span class="muted">No records available.</span>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListingComponent {
  @Input({ required: true }) columns: ColumnDef[] = [];
  @Input({ required: true }) data: any[] = [];
  @Input() actions: ActionDef[] = [];
  @Input() rowKey = 'id';
  @Output() actionTriggered = new EventEmitter<{ action: string; row: any }>();
  @Output() rowClicked = new EventEmitter<any>();

  trackBy(row: any): unknown {
    return row[this.rowKey] ?? JSON.stringify(row);
  }

  emitAction(event: Event, action: string, row: any): void {
    event.stopPropagation();
    this.actionTriggered.emit({ action, row });
  }

  isApproved(value: unknown): boolean {
    return String(value).includes('APPROVED');
  }

  isPending(value: unknown): boolean {
    return String(value).includes('PENDING');
  }

  isRejected(value: unknown): boolean {
    return String(value).includes('REJECTED');
  }
}
