import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class DraftService {
  private readonly api = inject(ApiService);

  save(surveyId: number, draftData: Record<string, unknown>) {
    localStorage.setItem(`draft_${surveyId}`, JSON.stringify(draftData));
    return this.api.saveDraft({ surveyId, draftData }).pipe(tap(() => void 0));
  }

  restore(surveyId: number): Observable<Record<string, unknown>> {
    return this.api.getDraft(surveyId).pipe(
      map((response) => response.draftData),
      catchError(() => {
        const local = localStorage.getItem(`draft_${surveyId}`);
        return of(local ? (JSON.parse(local) as Record<string, unknown>) : {});
      }),
    );
  }

  clear(surveyId: number) {
    localStorage.removeItem(`draft_${surveyId}`);
    return this.api.deleteDraft(surveyId);
  }
}
