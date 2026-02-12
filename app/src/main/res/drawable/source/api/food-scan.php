<?php
header("Content-Type: application/json");

$config = require __DIR__ . "/config.php";
$API_KEY = $config["GEMINI_API_KEY"];

/* 🔴 CHECK FILE */
if (!isset($_FILES["image"]) || $_FILES["image"]["error"] !== UPLOAD_ERR_OK) {
    echo json_encode(["error" => "Image file not provided"]);
    exit;
}

/* 🔴 READ FILE & CONVERT TO BASE64 */
$imageTmpPath = $_FILES["image"]["tmp_name"];
$imageMime    = mime_content_type($imageTmpPath);
$imageBase64  = base64_encode(file_get_contents($imageTmpPath));

/* 🔴 GEMINI PAYLOAD */
$payload = [
  "contents" => [[
    "parts" => [
      [
        "inlineData" => [
          "mimeType" => $imageMime,
          "data" => $imageBase64
        ]
      ],
      [
        "text" => "
Analyze the food image and return ONLY valid JSON.
Do NOT use markdown.
Do NOT wrap with ```.

{
  \"food_name\": \"\",
  \"category\": \"\",
  \"short_description\": \"\",
  \"diet_type\": [],
  \"serving_size\": \"\",
  \"nutrition\": {
    \"calories\": \"\",
    \"protein\": \"\",
    \"carbohydrates\": \"\",
    \"fat\": \"\",
    \"fiber\": \"\",
    \"sugar\": \"\"
  },
  \"sugar_level\": \"\",
  \"health_tags\": [],
  \"best_for\": [],
  \"not_good_for\": [],
  \"confidence_percentage\": \"\"
  \"Ingredients\": [],
   \"how_to_prepare\": [],
}

Rules:
- short_description: 1–2 short, user-friendly sentences
- sugar_level: Low / Medium / High
- not_good_for: health conditions or situations
- Use approximate realistic values
- Units: kcal and g
- JSON must be valid
"
      ]
    ]
  ]]
];

$url = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=".$API_KEY;

/* 🔴 CURL CALL */
$ch = curl_init($url);
curl_setopt_array($ch, [
  CURLOPT_RETURNTRANSFER => true,
  CURLOPT_POST => true,
  CURLOPT_HTTPHEADER => ["Content-Type: application/json"],
  CURLOPT_POSTFIELDS => json_encode($payload),
  CURLOPT_TIMEOUT => 40
]);

$response = curl_exec($ch);
curl_close($ch);

$gemini = json_decode($response, true);

/* 🔴 COLLECT ALL TEXT PARTS */
$text = "";
if (isset($gemini["candidates"][0]["content"]["parts"])) {
    foreach ($gemini["candidates"][0]["content"]["parts"] as $part) {
        if (isset($part["text"])) {
            $text .= $part["text"];
        }
    }
}

$text = trim($text);

if ($text === "") {
    echo json_encode([
        "error" => "Gemini returned empty text",
        "raw_response" => $gemini
    ]);
    exit;
}

/* 🔴 CLEAN MARKDOWN */
$text = preg_replace('/```json/i', '', $text);
$text = preg_replace('/```/i', '', $text);
$text = trim($text);

/* 🔴 PARSE JSON */
$data = json_decode($text, true);

if (!$data) {
    echo json_encode([
        "error" => "JSON parse failed",
        "raw_text" => $text
    ]);
    exit;
}

/* ✅ FINAL RESPONSE (SAME STRUCTURE) */
echo json_encode([
    "success" => true,
    "data" => $data
], JSON_PRETTY_PRINT);
