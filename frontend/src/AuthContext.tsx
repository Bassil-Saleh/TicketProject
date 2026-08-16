import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';

const JWT_STORAGE_KEY = 'ticketproject_jwt';

interface AuthContextValue {
    /** The current JWT, or null if not logged in. */
    jwt: string | null;
    /** Whether the user is currently logged in. */
    isLoggedIn: boolean;
    /** Store a JWT and mark the user as logged in. */
    login: (token: string) => void;
    /** Clear the JWT and mark the user as logged out. */
    logout: () => void;
    /**
     * Helper to make an authenticated fetch request with the JWT
     * attached as an Authorization: Bearer header.
     */
    authFetch: (url: string, options?: RequestInit) => Promise<Response>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

/**
 * AuthProvider wraps the application and provides authentication
 * state (JWT storage, login/logout helpers) to all child components.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
    const [jwt, setJwt] = useState<string | null>(() => {
        try {
            return localStorage.getItem(JWT_STORAGE_KEY);
        } catch {
            return null;
        }
    });

    const isLoggedIn = jwt !== null;

    const login = useCallback((token: string) => {
        setJwt(token);
        try {
            localStorage.setItem(JWT_STORAGE_KEY, token);
        } catch {
            // localStorage may be unavailable (e.g. private browsing);
            // the in-memory state still works for the current session.
        }
    }, []);

    const logout = useCallback(() => {
        setJwt(null);
        try {
            localStorage.removeItem(JWT_STORAGE_KEY);
        } catch {
            // Ignore storage errors on logout.
        }
    }, []);

    const authFetch = useCallback(
        (url: string, options: RequestInit = {}): Promise<Response> => {
            const headers = new Headers(options.headers);
            if (jwt) {
                headers.set('Authorization', `Bearer ${jwt}`);
            }
            if (!headers.has('Content-Type') && options.body) {
                headers.set('Content-Type', 'application/json');
            }
            return fetch(url, { ...options, headers });
        },
        [jwt]
    );

    return (
        <AuthContext.Provider value={{ jwt, isLoggedIn, login, logout, authFetch }}>
            {children}
        </AuthContext.Provider>
    );
}

/**
 * useAuth returns the current authentication context.
 * Must be used within an AuthProvider.
 */
export function useAuth(): AuthContextValue {
    const ctx = useContext(AuthContext);
    if (!ctx) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return ctx;
}