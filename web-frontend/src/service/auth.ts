import { useMemo } from 'react';
import { useOAuth2 } from '@tasoskakour/react-use-oauth2';
import ApiClient from './ApiClient';
import { toaster } from '@/components/ui/toaster';

const showError = (title: string, description: string) => {
  toaster.create({ title, description, type: 'error', duration: 5000 });
};

const showSuccess = (title: string, description: string, duration = 5000) => {
  toaster.create({ title, description, type: 'success', duration });
};

export const useAuth = () => {
  const { data, loading: authLoading, error: authError, getAuth } = useOAuth2({
    authorizeUrl: 'https://www.strava.com/api/v3/oauth/authorize',
    clientId: '4486',
    redirectUri: `${ApiClient.webHost}/oauth/strava`,
    scope: 'read,activity:read,profile:read_all',
    responseType: 'code',
    extraQueryParameters: { approval_prompt: 'auto' },
    exchangeCodeForTokenQuery: {
      url: `${ApiClient.apiHost}/api/token/strava`,
      method: 'POST',
    },
    onSuccess: (payload) => {
      localStorage.setItem('access_token', payload?.access_token);
      showSuccess('Connected to Strava', 'Successfully connected your Strava account!');
    },
    onError: (error_) => {
      console.error('Error', error_);
      showError('Connection Failed', 'Failed to connect to Strava. Please try again.');
    }
  });

  const isAuthenticated = useMemo(() => !!localStorage.getItem('access_token'), []);

  const logout = async () => {
    try {
      await ApiClient.logout();
      showSuccess('Logged out', 'You have been logged out successfully.', 4000);
    } catch (error) {
      console.error('Error during logout:', error);
      showError('Logout failed', 'We were unable to log you out. Please try again.');
    } finally {
      localStorage.removeItem('access_token');
      window.location.reload();
    }
  };

  return {
    isAuthenticated,
    authLoading,
    connect: getAuth,
    logout
  };
};