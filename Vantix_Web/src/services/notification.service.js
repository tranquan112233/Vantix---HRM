import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import api from '@/services/axios';

class NotificationService {
    stompClient = null;

    connect(userId, onMessageReceived) {
        const socket = new SockJS('/ws-hr');
        this.stompClient = Stomp.over(socket);
        this.stompClient.debug = (msg) => console.log('STOMP:', msg);

        this.stompClient.connect({}, () => {
            console.log('Connected with ID:', userId);
            // Đăng ký kênh nhận tin 1-1
            this.stompClient.subscribe(`/user/${userId}/queue/notifications`, (tick) => {
                if (tick.body) {
                    onMessageReceived(JSON.parse(tick.body));
                }
            });
        }, (err) => {
            console.error('WebSocket Error:', err);
            setTimeout(() => this.connect(userId, onMessageReceived), 5000);
        });
    }

    disconnect() {
        if (this.stompClient) this.stompClient.disconnect();
    }

    getMyNotifications(userId) {
        return api.get('/notifications/my', {params: {userId}});
    }

    markAsRead(id) {
        return api.put(`/notifications/${id}/read`);
    }

    toggleStar(id) {
        return api.put(`/notifications/${id}/star`);
    }

    delete(id) {
        return api.delete(`/notifications/${id}`);
    }

    markAllAsRead(userId) {
        return api.put('/notifications/read-all', null, {params: {userId}});
    }

    clearAllExceptStarred(userId) {
        return api.delete('/notifications/clear-all', {params: {userId}});
    }

    getRoles() {
        return api.get('/notifications/roles');
    }

    getRecipientList(roleName = 'ALL') {
        return api.get('/notifications/recipient-list', {params: {roleName}});
    }

    sendBulkSummon(data) {
        return api.post('/notifications/summon-bulk', data);
    }

    sendMultiSummon(data) {
        return api.post('/notifications/summon-multi', data);
    }
}
export default new NotificationService();
