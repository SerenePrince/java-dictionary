const tagsInput = document.getElementById('tagsInput');
const tagPreview = document.getElementById('tagPreview');
const tagInputs = document.getElementById('tagInputs');

if (tagsInput) {
    tagsInput.addEventListener('input', () => {
        const raw = tagsInput.value;
        const tags = raw.split(',')
            .map(t => t.trim())
            .filter(t => t.length > 0);

        tagPreview.innerHTML = tags
            .map(t => `<span class="badge me-1 mb-1">${t}</span>`)
            .join('');

        tagInputs.innerHTML = tags
            .map(t => `<input type="hidden" name="tags" value="${t}">`)
            .join('');
    });
}