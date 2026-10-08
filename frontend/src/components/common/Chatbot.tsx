import { useState, useEffect, useRef, useCallback } from 'react';
import { MessageSquare, Send, X, Minus } from 'lucide-react';
import { useToast } from '../../context/ToastContext';
import './Chatbot.css';

interface Message {
  text: string;
  isBot: boolean;
  timestamp: Date;
}

const CHATBOT_WS_URL =
  import.meta.env.VITE_CHATBOT_WS_URL || 'ws://localhost:8080/ws/chatbot';

const Chatbot = () => {
  const { showToast } = useToast();
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<Message[]>([
    { text: 'Merhaba! Ben SpaceNet dijital asistanı. Size nasıl yardımcı olabilirim?', isBot: true, timestamp: new Date() }
  ]);
  const [inputValue, setInputValue] = useState('');
  const [connected, setConnected] = useState(false);
  
  const socketRef = useRef<WebSocket | null>(null);
  const chatEndRef = useRef<HTMLDivElement>(null);

  const closeSocket = useCallback(() => {
    socketRef.current?.close();
    socketRef.current = null;
    setConnected(false);
  }, []);

  const connectWebSocket = useCallback(() => {
    if (socketRef.current) return;

    const socket = new WebSocket(CHATBOT_WS_URL);

    socket.onopen = () => {
      console.log('Chatbot connected');
      setConnected(true);
    };

    socket.onmessage = (event) => {
      try {
        const data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data;

        if (data.type === 'system') return;

        if (data.type === 'error') {
          showToast(data.message, 'error');
          return;
        }

        if (data.message) {
          setMessages((prev) => [...prev, {
            text: data.message,
            isBot: true,
            timestamp: new Date()
          }]);
        }
      } catch (err) {
        console.error('WebSocket parse error:', err);
      }
    };

    socket.onclose = () => {
      console.log('Chatbot disconnected');
      setConnected(false);
      socketRef.current = null;
    };

    socketRef.current = socket;
  }, [showToast]);

  useEffect(() => {
    if (isOpen && !socketRef.current) {
      connectWebSocket();
    }
    if (!isOpen && socketRef.current) {
      closeSocket();
    }
  }, [closeSocket, connectWebSocket, isOpen]);

  useEffect(() => {
    return () => {
      closeSocket();
    };
  }, [closeSocket]);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = () => {
    if (!inputValue.trim() || !socketRef.current || socketRef.current.readyState !== WebSocket.OPEN) return;

    const userMsg = { text: inputValue, isBot: false, timestamp: new Date() };
    setMessages(prev => [...prev, userMsg]);

    socketRef.current.send(JSON.stringify({ message: inputValue }));
    setInputValue('');
  };

  if (!isOpen) {
    return (
      <button className="chatbot-launcher glass" onClick={() => setIsOpen(true)}>
        <MessageSquare size={24} />
        <span className="launcher-text">SPACENET ASİSTAN</span>
      </button>
    );
  }

  return (
    <div className="chatbot-window glass">
      <div className="chatbot-header">
        <div className="bot-info">
          <div className="bot-avatar">🚀</div>
          <div>
            <h4>SpaceNet Asistan</h4>
            <span className="status">{connected ? 'Çevrimiçi' : 'Bağlanıyor...'}</span>
          </div>
        </div>
        <div className="header-actions">
          <button onClick={() => setIsOpen(false)}><Minus size={18} /></button>
          <button onClick={() => setIsOpen(false)}><X size={18} /></button>
        </div>
      </div>

      <div className="chatbot-messages">
        {messages.map((msg, i) => (
          <div key={i} className={`message-wrapper ${msg.isBot ? 'bot' : 'user'}`}>
            <div className="message-bubble">
              {msg.text}
              <span className="message-time">
                {msg.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </span>
            </div>
          </div>
        ))}
        <div ref={chatEndRef} />
      </div>

      <div className="chatbot-input">
        <input 
          type="text" 
          placeholder="Sorunuzu buraya yazın..." 
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSend()}
        />
        <button onClick={handleSend} disabled={!connected}>
          <Send size={20} />
        </button>
      </div>
    </div>
  );
};

export default Chatbot;
