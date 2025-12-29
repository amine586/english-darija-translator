document.addEventListener('DOMContentLoaded', () => {
    const sourceText = document.getElementById('sourceText');
    const translatedText = document.getElementById('translatedText');
    const translateBtn = document.getElementById('translateBtn');
    const copyBtn = document.getElementById('copyBtn');
    const status = document.getElementById('status');

    // Recevoir le texte sélectionné depuis content.js
    chrome.runtime.onMessage.addListener((message) => {
        if (message.action === 'textSelected') {
            sourceText.value = message.text;
            updateButtonState();
            showStatus('Texte sélectionné ! Cliquez sur "Traduire"', 'success');
        }
    });

    function updateButtonState() {
        translateBtn.disabled = sourceText.value.trim().length === 0;
    }

    function showStatus(message, type = '') {
        status.textContent = message;
        status.className = 'status';
        if (type) status.classList.add(type);
    }

    translateBtn.addEventListener('click', async () => {
        const text = sourceText.value.trim();
        if (!text) {
            showStatus('Veuillez sélectionner du texte', 'error');
            return;
        }

        translateBtn.disabled = true;
        showStatus('Traduction en cours...', 'loading');

        try {
            const response = await fetch('http://localhost:8082/darija-translator-service/api/translate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ text })
            });

            if (!response.ok) throw new Error(`Erreur HTTP: ${response.status}`);
            const data = await response.json();

            // Choisir la bonne clé dans la réponse
            translatedText.value = data.translatedText || data.text || data.content || data.response || JSON.stringify(data, null, 2);
            showStatus('Traduction réussie !', 'success');
        } catch (err) {
            console.error('Erreur de traduction:', err);
            translatedText.value = 'Erreur: ' + err.message;
            showStatus('Échec de la traduction', 'error');
        } finally {
            translateBtn.disabled = false;
        }
    });

    copyBtn.addEventListener('click', () => {
        const textToCopy = translatedText.value.trim();
        if (!textToCopy) {
            showStatus('Aucune traduction à copier', 'error');
            return;
        }

        navigator.clipboard.writeText(textToCopy).then(() => {
            showStatus('Traduction copiée !', 'success');
            copyBtn.style.transform = 'scale(0.95)';
            setTimeout(() => { copyBtn.style.transform = 'scale(1)'; }, 150);
        }).catch(err => {
            console.error('Erreur de copie:', err);
            showStatus('Erreur lors de la copie', 'error');
        });
    });

    updateButtonState();
});
