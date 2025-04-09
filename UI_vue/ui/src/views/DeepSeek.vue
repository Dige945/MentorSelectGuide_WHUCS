<template>
  <div class="chat-container">
    <div class="chat-header">
      <h1>大米集团</h1>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['chat-message', message.sender]"
      >
        {{ message.content }}
      </div>

      <div v-if="isBotResponding" class="typing-indicator">
        <div class="typing-dot"></div>
        <div class="typing-dot"></div>
        <div class="typing-dot"></div>
      </div>
    </div>

    <div class="chat-input">
      <input
          type="text"
          v-model="inputMessage"
          placeholder="输入消息..."
          @keydown.enter="handleSend"
          :disabled="isBotResponding"
      >
      <button
          @click="handleSend"
          :disabled="!inputMessage.trim() || isBotResponding"
      >
        <svg viewBox="0 0 24 24">
          <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
        </svg>
        发送
      </button>
    </div>
  </div>
</template>

<script>
import axios from "axios";
export default {
  name: 'ChatInterface',
  data() {
    return {
      inputMessage: '',
      messages: [],
      isBotResponding: false,
      currentBotMessage: null
    }
  },
  methods: {
    async handleSend() {
      if (!this.inputMessage.trim() || this.isBotResponding) return

      try {
        const userMessage = {
          sender: 'user',
          content: this.inputMessage.trim()
        }
        this.messages.push(userMessage)

        this.isBotResponding = true
        this.inputMessage = ''

        const botMessage = {
          sender: 'bot',
          content: ''
        }
        this.messages.push(botMessage)
        this.currentBotMessage = botMessage

        this.$nextTick(this.scrollToBottom)

        const response = await fetch('api/deepSeek/chat', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'text/event-stream'
          },
          body: JSON.stringify({ question: userMessage.content })
        })

        await this.handleStreamResponse(response)
      } catch (error) {
        console.error('请求失败:', error)
        this.messages.push({
          sender: 'bot',
          content: '暂时无法处理您的请求，请稍后再试'
        })
      } finally {
        this.isBotResponding = false
        this.currentBotMessage = null
      }
    },

    async handleStreamResponse(response) {
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      try {
        while (true) {
          const { done, value } = await reader.read()
          if (done) break

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          lines.forEach(line => {
            if (line.startsWith('data:')) {
              const data = line.replace(/^data:\s*/g, '').trim()
              if (data === '[DONE]') return
              this.currentBotMessage.content += data
              this.$forceUpdate()
              this.scrollToBottom()
            }
          })
        }
      } finally {
        if (buffer) {
          this.currentBotMessage.content += buffer
          this.$forceUpdate()
        }
      }
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    }
  }
}
</script>

<style scoped>
/* 全局变量需要放在非scoped样式或全局样式表中 */
.chat-container {
  --primary-color: #5b8cff;
  --user-bg: linear-gradient(135deg, #5b8cff 0%, #3d6ef7 100%);
  --bot-bg: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 100%);
  --shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.chat-container {
  width: 100%;
  max-width: 1000px;
  height: 85vh;
  min-height: 600px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: var(--shadow);
  display: flex;
  flex-direction: column;
  margin: 20px auto;
}

.chat-header {
  padding: 28px;
  background: var(--primary-color);
  color: white;
  border-radius: 20px 20px 0 0;
}

.chat-header h1 {
  margin: 0;
  font-size: 2.2rem;
  font-weight: 600;
}

.chat-messages {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chat-message {
  max-width: 85%;
  padding: 20px 28px;
  border-radius: 24px;
  font-size: 1.1rem;
  line-height: 1.6;
}

.chat-message.user {
  background: var(--user-bg);
  color: white;
  align-self: flex-end;
}

.chat-message.bot {
  background: var(--bot-bg);
  color: #2d3748;
  align-self: flex-start;
}

.chat-input {
  padding: 24px 30px;
  background: rgba(255, 255, 255, 0.9);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 16px;
}

.chat-input input {
  flex: 1;
  padding: 18px 28px;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 16px;
  font-size: 1.1rem;
}

.chat-input input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(91, 140, 255, 0.2);
}

.chat-input button {
  padding: 16px 36px;
  border-radius: 16px;
  font-size: 1.1rem;
  background: var(--primary-color);
  transition: transform 0.2s;
}

.chat-input button:hover {
  transform: translateY(-2px);
}

.typing-indicator {
  padding: 16px 24px;
  gap: 10px;
}

.typing-dot {
  width: 10px;
  height: 10px;
}

@media (max-width: 768px) {
  .chat-container {
    max-width: 100%;
    height: 90vh;
    min-height: auto;
    margin: 10px;
    border-radius: 16px;
  }

  .chat-header {
    padding: 20px;
  }

  .chat-header h1 {
    font-size: 1.8rem;
  }

  .chat-messages {
    padding: 20px;
    gap: 16px;
  }

  .chat-message {
    max-width: 90%;
    padding: 16px 20px;
    font-size: 1rem;
  }

  .chat-input {
    padding: 20px;
  }

  .chat-input input {
    padding: 14px 20px;
    font-size: 1rem;
  }

  .chat-input button {
    padding: 12px 24px;
    font-size: 1rem;
  }
}
</style>