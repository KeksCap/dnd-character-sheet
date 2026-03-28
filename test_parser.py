import json
import re
import os

def parse_class_md(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find the start of subclasses (usually after Level 20 feature)
    # Or we can split by the characteristic intros.
    
    # Base class features usually end at Level 20.
    # We'll split the file into "Base" and "Subclasses"
    parts = re.split(r'### Уровень 20:.*\n', content)
    if len(parts) < 2:
        # Fallback: find the first line after Level 20
        parts = re.split(r'Уровень 20:.*\n', content)
    
    if len(parts) < 2:
        print(f"Could not find Level 20 boundary in {filepath}")
        subclasses_text = content # Try parsing everything
    else:
        # We take everything after the Level 20 feature description
        # Usually there's some text after the Level 20 header.
        subclasses_text = parts[1]

    # Subclasses often start with a catchy line and then features.
    # Let's split by the '### Уровень 3' which identifies a new subclass usually
    # But wait, a subclass has features at levels 3, 6, 10, 14 etc.
    
    # Better approach: Find all headers '### Уровень X: ...'
    # and the text between them.
    
    # However, we need to group them by subclass.
    # In next.dnd.su, a subclass starts with a description text.
    
    # Let's use the intro lines as markers.
    # A subclass intro is roughly a paragraph at the start of a subclass section.
    
    # I'll use a more manual split based on the identified patterns:
    # Fighter: 'Освойте...', 'Поддерживайте...', 'Умножайте...', 'Добивайтесь...', 'Вдохновляющим...'
    # Barbarian: 'Взрастите...', 'Будьте в единстве...', 'Путешествуйте...', 'Бушуйте...', 'Называемые...'
    
    subclasses = []
    
    # Regex to find features
    feature_regex = r'### Уровень (\d+): (.*?)\n(.*?)(?=\n###|\n\n[А-ЯA-Z]|\Z)'
    
    # We'll split the subclasses_text by the intro headers.
    # Since intro headers are just plain text paragraphs, it's tricky.
    
    # Let's try to find all '### Уровень 3' and use them as subclass starters.
    subclass_starts = [m.start() for m in re.finditer(r'### Уровень 3:', subclasses_text)]
    
    for i in range(len(subclass_starts)):
        start = subclass_starts[i]
        end = subclass_starts[i+1] if i+1 < len(subclass_starts) else len(subclasses_text)
        
        # The description is usually the text BEFORE the first Level 3 feature but AFTER the previous subclass.
        # For the first subclass, it's after the Level 20 feature.
        intro_start = 0 if i == 0 else subclass_starts[i-1] # placeholder
        # Actually, let's look at the text immediately preceding the current start.
        preceding_text = subclasses_text[:start].strip().split('\n')
        # The last few lines of preceding text (that aren't part of the previous subclass features)
        # constitute the current subclass intro.
        
        # This is getting complex. Let's use a simpler heuristic:
        # Each subclass segment starts with an intro and then has features.
        segment = subclasses_text[start:end]
        
        # Name and Main Desc?
        # The name is often in the first feature or the intro.
        # Actually, let's just extract all features and group them.
        
        features = []
        for match in re.finditer(feature_regex, segment, re.DOTALL):
            level = int(match.group(1))
            name = match.group(2).strip()
            desc = match.group(3).strip()
            features.append({
                "level": level,
                "name": name,
                "desc": desc
            })
            
        if features:
            # Try to guess name from the first feature or something
            # I'll refine names manually or via mapping.
            subclasses.append({
                "features": features
            })
            
    return subclasses

# Mapping for 2024 PHB Subclasses
# This will help me name the parsed subclasses correctly.
subclass_map = {
    "barbarian": ["Berserker", "Wild Heart", "World Tree", "Zealot"],
    "fighter": ["Battle Master", "Eldritch Knight", "Psi Warrior", "Champion"],
    "rogue": ["Arcane Trickster", "Assassin", "Soulknife", "Thief"],
    # ... will expand
}

# For now, let's just run it on one file and see.
print(json.dumps(parse_class_md('d:/Android_projects/DnD/tmp_2024_pages/fighter.md'), ensure_ascii=False, indent=2))
