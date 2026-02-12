
// Set current year in footer
document.getElementById('currentYear').textContent = new Date().getFullYear();

// State management
let appState = {
    lastResultData: null,
    baseNutrition: {},
    currentServing: 1,
    fileInput: null,
    uploadBox: null,
    previewBox: null,
    previewImg: null,
    heroSection: null,
    resultSection: null
};

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Cache DOM elements
    appState.fileInput = document.getElementById('fileInput');
    appState.uploadBox = document.getElementById('uploadBox');
    appState.previewBox = document.getElementById('previewBox');
    appState.previewImg = document.getElementById('previewImg');
    appState.heroSection = document.querySelector('.hero');
    appState.resultSection = document.getElementById('resultSection');
    
    // Initialize file input
    if (appState.fileInput) {
        appState.fileInput.addEventListener('change', handleFileSelect);
    }
    
    // Initialize drag and drop
    if (appState.uploadBox) {
        initDragAndDrop();
    }
    
    // Initialize FAQ keyboard support
    initFAQs();
});

/* FAQ TOGGLE */
function toggleFaq(element){
    const answer = element.nextElementSibling;
    const icon = element.querySelector("span");
    const isExpanded = answer.classList.contains("active");
    
    answer.classList.toggle("active");
    icon.textContent = isExpanded ? "+" : "-";
    element.setAttribute('aria-expanded', !isExpanded);
}

function initFAQs() {
    document.querySelectorAll('.faq-question').forEach(question => {
        question.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                toggleFaq(e.target);
            }
        });
    });
}

/* SCROLL FUNCTIONS */
function scrollToUpload() {
    document.querySelector('.hero-upload').scrollIntoView({ 
        behavior: 'smooth',
        block: 'center'
    });
}

function scrollToHowItWorks() {
    document.getElementById('how-it-works').scrollIntoView({ 
        behavior: 'smooth'
    });
}

/* FILE UPLOAD */
function openFile(){
    if (appState.fileInput) {
        appState.fileInput.value = "";
        appState.fileInput.click();
    }
}

function handleFileSelect() {
    const file = appState.fileInput.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
        alert('Please select an image file (JPEG, PNG, etc.)');
        return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
        appState.previewImg.src = e.target.result;
        appState.uploadBox.style.display = "none";
        appState.previewBox.style.display = "block";
        appState.previewBox.hidden = false;
    };
    reader.readAsDataURL(file);
}



/* ANALYZE FOOD */
async function analyzeFood(){
    const file = appState.fileInput.files[0];
    if (!file) {
        alert("Please select an image first");
        return;
    }

    const analyzeBtn = document.getElementById("analyzeBtn");
    if (!analyzeBtn) return;

    // Save original state
    const originalText = analyzeBtn.innerHTML;
    const originalDisabled = analyzeBtn.disabled;

    analyzeBtn.innerHTML = `
        <span class="btn-loading">
            <span class="btn-spinner"></span>
            Analyzing food...
        </span>
    `;
    analyzeBtn.disabled = true;

    const formData = new FormData();
    formData.append("image", file);

    try {
        // In production, replace with your actual API endpoint
        const res = await fetch(
            "https://placementinsider.com/Food-detector/source/api/food-scan.php",
            { 
                method: "POST", 
                body: formData,
                headers: {
                    'Accept': 'application/json'
                }
            }
        );

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const json = await res.json();

        if (json.success) {
            renderResult(json.data);
        } else {
            alert("Analysis failed. Please try again with a clearer photo.");
        }
    } catch (err) {
        console.error('Analysis error:', err);
        
        // Fallback mock data for demo purposes
        if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
            renderResult(getMockData());
        } else {
            alert("Server error. Please check your connection and try again.");
        }
    } finally {
        analyzeBtn.innerHTML = originalText;
        analyzeBtn.disabled = originalDisabled;
    }
}

/* RENDER RESULT */
function renderResult(data) {
    appState.lastResultData = data;
    appState.heroSection.style.display = "none";
    appState.resultSection.style.display = "block";
    appState.resultSection.hidden = false;

    // Update UI elements safely
    updateElementText('resultTitle', data.food_name || 'Unknown Food');
    updateElementSrc('resultImage', appState.previewImg.src);
    updateElementText('resultDesc', data.short_description || '');
    updateElementText('servingText', data.serving_size || '');
    updateElementText('sugarlevel', `Sugar Level: ${data.sugar_level || 'N/A'}`);
    updateElementText('confidenceNote', `Confidence Level: ${data.confidence_percentage || 'N/A'}`);

    // Update tags
    updateTags('resultTags', [data.category || '', ...(data.diet_type || [])]);
    updateTags('healthTags', data.health_tags || []);
    updateTags('bestFor', data.best_for || []);
    updateTags('notGoodFor', data.not_good_for || []);

    // Base nutrition (1 serving)
    appState.baseNutrition = {
        calories: parseFloat(data.nutrition?.calories || 0),
        protein: parseFloat(data.nutrition?.protein || 0),
        carbs: parseFloat(data.nutrition?.carbohydrates || 0),
        fat: parseFloat(data.nutrition?.fat || 0),
        fiber: parseFloat(data.nutrition?.fiber || 0),
        sugar: parseFloat(data.nutrition?.sugar || 0)
    };

    // Reset serving to 1
    appState.currentServing = 1;
    updateElementText('servingCount', appState.currentServing);

    // Update nutrition grid
    updateNutritionGrid();

    // Update ingredients
    updateIngredients(data.Ingredients || []);

    // Update preparation steps
    updatePreparationSteps(data.how_to_prepare || []);

    // Smooth scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Helper functions for safe DOM updates
function updateElementText(id, text) {
    const element = document.getElementById(id);
    if (element) element.textContent = text;
}

function updateElementSrc(id, src) {
    const element = document.getElementById(id);
    if (element) element.src = src;
}

function updateTags(id, tags) {
    const container = document.getElementById(id);
    if (!container) return;
    
    container.innerHTML = tags
        .filter(tag => tag && tag.trim())
        .map(tag => `<span class="tag">${tag}</span>`)
        .join('');
}

function updateIngredients(ingredients) {
    const container = document.getElementById('ingredients');
    if (!container) return;
    
    container.innerHTML = `
        <ul>
            ${ingredients.map(ing => `<li>${ing}</li>`).join('')}
        </ul>
    `;
}

function updatePreparationSteps(steps) {
    const container = document.getElementById('howto');
    if (!container) return;
    
    container.innerHTML = steps
        .map((step, index) => `<li>${step}</li>`)
        .join('');
}

/* UPDATE NUTRITION GRID */
function updateNutritionGrid() {
    const container = document.getElementById('nutritionGrid');
    if (!container) return;

    const s = appState.currentServing;
    const nut = appState.baseNutrition;

    container.innerHTML = `
        <div class="nutrition-item cal">
            Calories<br><b>${(nut.calories * s).toFixed(0)} kcal</b>
        </div>
        <div class="nutrition-item pro">
            Protein<br><b>${(nut.protein * s).toFixed(1)} g</b>
        </div>
        <div class="nutrition-item carb">
            Carbs<br><b>${(nut.carbs * s).toFixed(1)} g</b>
        </div>
        <div class="nutrition-item fat">
            Fat<br><b>${(nut.fat * s).toFixed(1)} g</b>
        </div>
        <div class="nutrition-item fiber">
            Fiber<br><b>${(nut.fiber * s).toFixed(1)} g</b>
        </div>
        <div class="nutrition-item sugar">
            Sugar<br><b>${(nut.sugar * s).toFixed(1)} g</b>
        </div>
    `;
}

/* CHANGE SERVING */
function changeServing(delta) {
    const next = appState.currentServing + delta;
    if (next < 0.25) return; // Minimum 0.25 servings

    appState.currentServing = next;
    updateElementText('servingCount', appState.currentServing.toFixed(2));
    updateNutritionGrid();
}

/* GO BACK */
function goBack() {
    appState.resultSection.style.display = "none";
    appState.resultSection.hidden = true;
    appState.heroSection.style.display = "block";
    window.scrollTo({ top: 0, behavior: 'smooth' });
}


/* EXPORT PDF */
function exportPDF() {
    let lastResultData =  appState.lastResultData
    if (!lastResultData) {
        alert("No data to export");
        return;
    }

    const { jsPDF } = window.jspdf;
    const doc = new jsPDF("p", "mm", "a4");

    const pageWidth = 210;
    const pageHeight = 297;
    const margin = 18;
    const contentWidth = pageWidth - margin * 2;
    let y = 22;

    const lineGap = 6;

    /* ===== HELPERS ===== */
    const newPageIfNeeded = (extra = 0) => {
        if (y + extra > pageHeight - 20) {
            doc.addPage();
            y = 22;
        }
    };

    const sectionTitle = (text) => {
        newPageIfNeeded(20);
        doc.setFontSize(15);
        doc.setTextColor(20);
        doc.text(text, margin, y);
        y += 4;
        doc.setDrawColor(220);
        doc.line(margin, y, margin + contentWidth, y);
        y += 10;
    };

    /* ===== HEADER ===== */
    doc.setFontSize(20);
    doc.setTextColor(15);
    doc.text(lastResultData.food_name, margin, y);
    y += 6;

    doc.setFontSize(10);
    doc.setTextColor(120);
    doc.text(
        `Category: ${lastResultData.category}  |  Serving: ${lastResultData.serving_size}`,
        margin,
        y
    );
    y += 12;

    /* ===== IMAGE (NO STRETCH) ===== */
    const img = previewImg.src;
    const imgProps = doc.getImageProperties(img);
    const imgRatio = imgProps.width / imgProps.height;

    const imgBoxHeight = 60;
    let imgWidth = contentWidth;
    let imgHeight = imgBoxHeight;

    if (imgWidth / imgHeight > imgRatio) {
        imgWidth = imgHeight * imgRatio;
    } else {
        imgHeight = imgWidth / imgRatio;
    }

    const imgX = margin + (contentWidth - imgWidth) / 2;

    doc.setDrawColor(220);
    doc.roundedRect(margin, y, contentWidth, imgBoxHeight + 6, 3, 3);
    doc.addImage(img, "JPEG", imgX, y + 3, imgWidth, imgHeight);
    y += imgBoxHeight + 14;

    /* ===== OVERVIEW ===== */
    sectionTitle("Overview");

    doc.setFontSize(11);
    doc.setTextColor(60);
    const desc = doc.splitTextToSize(
        lastResultData.short_description,
        contentWidth
    );
    doc.text(desc, margin, y);
    y += desc.length * lineGap + 12;

    /* ===== NUTRITION ===== */
    sectionTitle("Nutrition Summary (per serving)");

    doc.setFontSize(11);
    doc.setTextColor(50);

    const nutrition = lastResultData.nutrition;
    const rows = [
        ["Calories", nutrition.calories],
        ["Protein", nutrition.protein],
        ["Carbohydrates", nutrition.carbohydrates],
        ["Fat", nutrition.fat],
        ["Fiber", nutrition.fiber],
        ["Sugar", nutrition.sugar]
    ];

    rows.forEach(([label, value]) => {
        newPageIfNeeded(12);
        doc.setDrawColor(235);
        doc.rect(margin, y - 5, contentWidth, 9);
        doc.text(label, margin + 4, y);
        doc.setFont(undefined, "bold");
        doc.text(value.toString(), margin + contentWidth - 4, y, { align: "right" });
        doc.setFont(undefined, "normal");
        y += 11;
    });

    y += 10;

    /* ===== META ===== */
    newPageIfNeeded(20);
    doc.setFontSize(10);
    doc.setTextColor(90);
    doc.text(`Sugar Level: ${lastResultData.sugar_level}`, margin, y);
    y += 6;
    doc.text(`Confidence Level: ${lastResultData.confidence_percentage}`, margin, y);

    /* ===== INGREDIENTS ===== */
    doc.addPage();
    y = 22;

    sectionTitle("Ingredients");

    doc.setFontSize(11);
    doc.setTextColor(60);

    lastResultData.Ingredients.forEach(item => {
        newPageIfNeeded(10);
        doc.text("•", margin, y);
        doc.text(item, margin + 5, y);
        y += lineGap + 3;
    });

y += 12;   // <-- this is your margin-top

newPageIfNeeded(20);
sectionTitle("Preparation Method");

doc.setFontSize(11);
doc.setTextColor(60);

lastResultData.how_to_prepare.forEach((step, i) => {
    const text = doc.splitTextToSize(
        `${i + 1}. ${step}`,
        contentWidth
    );

    newPageIfNeeded(text.length * lineGap + 6);
    doc.text(text, margin, y);
    y += text.length * lineGap + 6;
});
    /* ===== FOOTER ===== */
    const pages = doc.getNumberOfPages();
    for (let i = 1; i <= pages; i++) {
        doc.setPage(i);
        doc.setFontSize(9);
        doc.setTextColor(150);
        doc.text(
            `FoodScan AI • Nutrition Report • Page ${i} of ${pages}`,
            pageWidth / 2,
            290,
            { align: "center" }
        );
    }

    doc.save("FoodScanerAI_Report.pdf");
}

/* MOCK DATA FOR DEMO */
function getMockData() {
    return {
        food_name: "Avocado Toast",
        short_description: "Healthy avocado toast with whole grain bread, fresh avocado, and seasoning.",
        serving_size: "1 slice (approx 100g)",
        sugar_level: "Low",
        confidence_percentage: "92%",
        category: "Breakfast",
        diet_type: ["Vegetarian", "Healthy"],
        nutrition: {
            calories: "250",
            protein: "8",
            carbohydrates: "22",
            fat: "15",
            fiber: "7",
            sugar: "2"
        },
        health_tags: ["Heart Healthy", "High Fiber", "Rich in Vitamins"],
        best_for: ["Weight Loss", "Breakfast", "Healthy Snack"],
        not_good_for: ["Low Fat Diet", "Keto Diet"],
        Ingredients: ["Whole grain bread", "Avocado", "Lemon juice", "Sea salt", "Black pepper", "Red pepper flakes"],
        how_to_prepare: [
            "Toast the whole grain bread until crispy",
            "Mash the avocado in a bowl with lemon juice, salt, and pepper",
            "Spread the avocado mixture evenly on the toast",
            "Sprinkle with red pepper flakes and serve immediately"
        ]
    };
}

// Keyboard navigation support
document.addEventListener('keydown', (e) => {
    // Escape key to go back from results
    if (e.key === 'Escape' && appState.resultSection.style.display === 'block') {
        goBack();
    }
    
    // Enter key to trigger file selection when upload box is focused
    if (e.key === 'Enter' && document.activeElement === appState.uploadBox) {
        openFile();
    }
});

// Add loading state for better UX
window.addEventListener('load', () => {
    document.body.classList.add('loaded');
});

// Error handling for image loading
if (appState.previewImg) {
    appState.previewImg.onerror = () => {
        alert('Error loading image. Please try a different file.');
        appState.uploadBox.style.display = "block";
        appState.previewBox.style.display = "none";
        appState.previewBox.hidden = true;
    };
}
