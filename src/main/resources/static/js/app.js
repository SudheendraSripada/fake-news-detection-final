const API_BASE_URL = '/api/news';

// Form submission handler
document.getElementById('newsForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const title = document.getElementById('title').value.trim();
    const content = document.getElementById('content').value.trim();
    const submitBtn = document.getElementById('submitBtn');
    const btnText = document.getElementById('btnText');
    const btnLoading = document.getElementById('btnLoading');

    // Disable button and show loading state
    submitBtn.disabled = true;
    btnText.classList.add('hidden');
    btnLoading.classList.remove('hidden');

    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ title, content })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

        // Display result
        displayResult(data);

        // Clear form
        document.getElementById('title').value = '';
        document.getElementById('content').value = '';

        // Refresh news list
        await loadNewsList();

    } catch (error) {
        console.error('Error:', error);
        alert('❌ Failed to check news. Please make sure the server is running and try again.');
    } finally {
        // Re-enable button
        submitBtn.disabled = false;
        btnText.classList.remove('hidden');
        btnLoading.classList.add('hidden');
    }
});

// Display result
function displayResult(data) {
    const resultDiv = document.getElementById('result');
    resultDiv.classList.remove('hidden', 'fake', 'real');

    if (data.fake) {
        resultDiv.classList.add('fake');
        resultDiv.innerHTML = '⚠️ <strong>FAKE NEWS DETECTED!</strong><br>This content contains suspicious patterns commonly found in fake news.';
    } else {
        resultDiv.classList.add('real');
        resultDiv.innerHTML = '✅ <strong>APPEARS TO BE REAL NEWS</strong><br>No suspicious patterns detected in this content.';
    }

    // Scroll to result
    resultDiv.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// Load and display news list
async function loadNewsList() {
    try {
        const response = await fetch(API_BASE_URL);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const newsList = await response.json();
        const newsListDiv = document.getElementById('newsList');

        if (newsList.length === 0) {
            newsListDiv.innerHTML = '<p style="text-align: center; color: #999;">No news checked yet. Be the first to check some news!</p>';
            return;
        }

        // Display news in reverse order (newest first)
        newsListDiv.innerHTML = newsList.slice().reverse().map(news => `
            <div class="news-item">
                <h3>${escapeHtml(news.title)}</h3>
                <p>${escapeHtml(truncateText(news.content, 200))}</p>
                <span class="status ${news.fake ? 'fake' : 'real'}">
                    ${news.fake ? '❌ Fake' : '✅ Real'}
                </span>
            </div>
        `).join('');

    } catch (error) {
        console.error('Error loading news list:', error);
        document.getElementById('newsList').innerHTML =
            '<p style="text-align: center; color: #ff6b6b;">⚠️ Error loading news. Please refresh the page.</p>';
    }
}

// Helper function to truncate text
function truncateText(text, maxLength) {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

// Helper function to escape HTML (prevent XSS)
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Load news list when page loads
document.addEventListener('DOMContentLoaded', function() {
    loadNewsList();
});
