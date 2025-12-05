## Configuração de CD com Self-Hosted Runner (Persistente)

### 1. Configurar o Runner
1. No repositório GitHub, ir a **Settings** > **Actions** > **Runners**.
2. Clicar em **New self-hosted runner**.
3. Selecionar o sistema operativo (Linux) e correr os comandos fornecidos na VM para:
   - Baixar o pacote (`curl ...`).
   - Extrair o instalador (`tar ...`).
   - Configurar o runner (`./config.sh ...`).

### 2. Instalar como Serviço (Persistência)
Em vez de usar `./run.sh` (que para se fechar o terminal), usamos o `svc.sh` para instalar o runner como um serviço do sistema (systemd). Isto garante que o runner está sempre ativo.

**Comandos:**

*   **Instalar o serviço:**
    ```bash
    sudo ./svc.sh install
    ```
    *Cria os ficheiros de serviço systemd.*

*   **Iniciar o serviço:**
    ```bash
    sudo ./svc.sh start
    ```
    *Inicia o runner em background.*

*   **Verificar estado:**
    ```bash
    sudo ./svc.sh status
    ```
    *Mostra se o serviço está `active (running)`.*

*   **Parar o serviço:**
    ```bash
    sudo ./svc.sh stop
    ```

*   **Desinstalar:**
    ```bash
    sudo ./svc.sh uninstall
    ```


