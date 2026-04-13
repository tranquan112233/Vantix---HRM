import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

class NotificationService {
    stompClient = null;

    connect(userId, onMessageReceived) {
        const socket = new SockJS('http://localhost:8080/ws-hr');
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
}
export default new NotificationService();