const tagsInput = document.getElementById('tagsInput');
const tagPreview = document.getElementById('tagPreview');
const tagInputs = document.getElementById('tagInputs');

if (tagsInput) {
    tagsInput.addEventListener('input', () => {
        const raw = tagsInput.value;
        const tags = raw.split(',')
            .map(t => t.trim())
            .filter(t => t.length > 0);

        tagPreview.innerHTML = '';
        tags.forEach(t => {
            const span = document.createElement('span');
            span.className = 'badge me-1 mb-1';
            span.textContent = t;
            tagPreview.appendChild(span);
        });

        tagInputs.innerHTML = '';
        tags.forEach(t => {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'tags';
            input.value = t;
            tagInputs.appendChild(input);
        });
    });
}