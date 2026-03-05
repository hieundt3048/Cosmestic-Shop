import { useEffect } from 'react';
import { reportAPI } from '../api/reportApi';

const HEARTBEAT_INTERVAL_MS = 60 * 1000;

const UserActivityTracker = () => {
  useEffect(() => {
    const pingActivity = async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        return;
      }

      try {
        await reportAPI.pingActivity();
      } catch {
        // Silent fail để không ảnh hưởng trải nghiệm người dùng
      }
    };

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        pingActivity();
      }
    };

    pingActivity();
    const intervalId = window.setInterval(pingActivity, HEARTBEAT_INTERVAL_MS);
    window.addEventListener('focus', pingActivity);
    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      window.clearInterval(intervalId);
      window.removeEventListener('focus', pingActivity);
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, []);

  return null;
};

export default UserActivityTracker;
