import { useEffect, useRef, useState } from 'react';
import chatbotApi from '../api/chatbotApi';

const QUICK_QUESTIONS = [
  'Gói bảo hiểm xe máy có giá bao nhiêu?',
  'Khai báo tai nạn thế nào?',
  'Điều kiện để được bồi thường?',
  'Gia hạn hợp đồng khi nào?',
];

export default function ChatWidget() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'ai',
      text: 'Xin chào! Tôi là Trợ lý AI Bảo hiểm Xe máy. Tôi có thể giúp bạn giải đáp bảng giá, quy trình khai báo tai nạn hoặc thủ tục bồi thường. Bạn cần hỗ trợ gì?',
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [messages, isOpen]);

  const handleSend = async (textToSend) => {
    const text = (textToSend || input).trim();
    if (!text || loading) return;

    const userMessage = {
      id: Date.now(),
      sender: 'user',
      text,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setLoading(true);

    try {
      const res = await chatbotApi.chat(text);
      const aiReply = {
        id: Date.now() + 1,
        sender: 'ai',
        text: res.reply,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        source: res.source,
      };
      setMessages((prev) => [...prev, aiReply]);
    } catch {
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          sender: 'ai',
          text: 'Rất tiếc, đã có lỗi kết nối đến trợ lý ảo. Vui lòng thử lại sau giây lát!',
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="chat-widget-wrapper">
      {/* Nút tròn nổi mở Chat */}
      {!isOpen && (
        <button
          type="button"
          className="chat-toggle-btn shadow-lg"
          onClick={() => setIsOpen(true)}
          title="Chat với Trợ lý AI"
        >
          <i className="bi bi-robot fs-3" />
          <span className="chat-badge">AI</span>
        </button>
      )}

      {/* Cửa sổ Chat */}
      {isOpen && (
        <div className="chat-window card shadow-lg border-0">
          <div className="card-header bg-primary text-white d-flex align-items-center justify-content-between py-2 px-3">
            <div className="d-flex align-items-center gap-2">
              <i className="bi bi-robot fs-4" />
              <div>
                <strong className="d-block" style={{ fontSize: '0.95rem' }}>Trợ lý AI Bảo hiểm</strong>
                <small style={{ fontSize: '0.75rem', opacity: 0.9 }}>
                  <i className="bi bi-dot text-success fs-6" /> Tư vấn 24/7 trực tuyến
                </small>
              </div>
            </div>
            <button
              type="button"
              className="btn btn-sm btn-link text-white p-0"
              onClick={() => setIsOpen(false)}
              aria-label="Đóng chat"
            >
              <i className="bi bi-x-lg fs-5" />
            </button>
          </div>

          <div className="card-body p-3 chat-messages-container">
            {messages.map((m) => (
              <div
                key={m.id}
                className={`d-flex flex-column mb-3 ${m.sender === 'user' ? 'align-items-end' : 'align-items-start'}`}
              >
                <div
                  className={`chat-bubble ${
                    m.sender === 'user' ? 'chat-bubble-user bg-primary text-white' : 'chat-bubble-ai bg-light text-dark'
                  }`}
                >
                  <div style={{ whiteSpace: 'pre-wrap', lineHeight: 1.45 }}>{m.text}</div>
                </div>
                <small className="text-muted mt-1 px-1" style={{ fontSize: '0.7rem' }}>
                  {m.time} {m.source === 'GEMINI_AI' && <span className="badge bg-secondary ms-1">Gemini AI</span>}
                </small>
              </div>
            ))}

            {loading && (
              <div className="d-flex align-items-center gap-2 text-muted my-2">
                <i className="bi bi-robot fs-5 text-primary" />
                <div className="spinner-border spinner-border-sm text-primary" role="status" />
                <small style={{ fontStyle: 'italic' }}>AI đang suy nghĩ...</small>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Gợi ý câu hỏi nhanh */}
          <div className="px-2 py-1 border-top bg-light chat-quick-suggestions">
            <small className="text-muted d-block mb-1" style={{ fontSize: '0.72rem' }}>
              Gợi ý câu hỏi:
            </small>
            <div className="d-flex gap-1 overflow-auto pb-1" style={{ whiteSpace: 'nowrap' }}>
              {QUICK_QUESTIONS.map((q, i) => (
                <button
                  key={i}
                  type="button"
                  className="btn btn-outline-secondary btn-sm rounded-pill py-0 px-2"
                  style={{ fontSize: '0.75rem' }}
                  onClick={() => handleSend(q)}
                  disabled={loading}
                >
                  {q}
                </button>
              ))}
            </div>
          </div>

          {/* Ô nhập tin nhắn */}
          <div className="card-footer p-2 bg-white border-top">
            <div className="input-group">
              <input
                type="text"
                className="form-control form-control-sm"
                placeholder="Nhập câu hỏi cần tư vấn..."
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                disabled={loading}
              />
              <button
                type="button"
                className="btn btn-primary btn-sm px-3"
                onClick={() => handleSend()}
                disabled={!input.trim() || loading}
              >
                <i className="bi bi-send-fill" />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
