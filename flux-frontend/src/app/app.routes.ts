import { Routes } from '@angular/router';
import { KanbanPageComponent } from './features/board/kanban-page/kanban-page.component';
import { AppLayoutComponent } from './shared/layout/app-layout/app-layout.component';
import { BoardManagementPageComponent } from './features/board/board-management-page/board-management-page.component';
import { ProfilePageComponent } from './features/user/profile-page/profile-page.component';
import { LoginPageComponent } from './features/user/login-page/login-page.component';
import { LoginGuard } from './core/guard/login.guard';
import { RegisterPageComponent } from './features/user/register-page/register-page.component';
import { AuthGuard } from './core/guard/auth.guard';

export const routes: Routes = [
    {
        path: 'login',
        component: LoginPageComponent,
        canActivate: [LoginGuard]
    },
    {
        path: 'register',
        component: RegisterPageComponent,
        canActivate: [LoginGuard]
    },
    {
        path: '',
        component: AppLayoutComponent,
        children: [
            { path: '', redirectTo: 'board', pathMatch: 'full' },
            { path: 'board', component: BoardManagementPageComponent, canActivate: [AuthGuard] },
            {
                path: 'boards/:id',
                component: KanbanPageComponent,
                canActivate: [AuthGuard]
            },
            { path: 'settings', component: ProfilePageComponent, canActivate: [AuthGuard] }
        ]
    },
    { path: '**', redirectTo: 'login' }
];
