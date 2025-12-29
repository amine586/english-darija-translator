chrome.action.onClicked.addListener(async (tab) => {
    try {
        await chrome.sidePanel.open({ tabId: tab.id });
    } catch (err) {
        console.error('Erreur en ouvrant le side panel:', err);
    }
});
