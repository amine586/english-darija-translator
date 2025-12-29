let lastSelectedText = '';

document.addEventListener('mouseup', () => {
    const selectedText = window.getSelection().toString().trim();

    if (selectedText && selectedText !== lastSelectedText) {
        lastSelectedText = selectedText;

        // Envoi au background (service worker) ou sidebar
        chrome.runtime.sendMessage({ action: 'textSelected', text: selectedText }, () => {
            // On ignore l'erreur si le service worker n'est pas encore prêt
            if (chrome.runtime.lastError) {
                console.log('Erreur en envoyant le message:', chrome.runtime.lastError.message);
            }
        });
    }
});

// Réponse aux requêtes de texte depuis le sidebar
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.action === 'getSelectedText') {
        const selectedText = window.getSelection().toString().trim();
        sendResponse({ text: selectedText || '' });
        // Pas besoin de return true car la réponse est immédiate
    }
});
