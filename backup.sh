#!/bin/bash

# Script di backup per il progetto vaadin-examples
# Crea un archivio tar.gz escludendo file non necessari

# Colori per output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Backup Script per vaadin-examples ===${NC}"

# Directory di backup
BACKUP_DIR="/workspace/db-ready/backup"
PROJECT_DIR="/workspace/db-ready/vaadin-examples"

# Crea directory di backup se non esiste
if [ ! -d "$BACKUP_DIR" ]; then
    echo -e "${GREEN}Creazione directory backup: $BACKUP_DIR${NC}"
    mkdir -p "$BACKUP_DIR"
fi

# Genera nome file con data e ora
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="vaadin-examples_backup_${TIMESTAMP}.tar.gz"
BACKUP_PATH="${BACKUP_DIR}/${BACKUP_FILE}"

echo -e "${BLUE}Data backup: $(date +"%Y-%m-%d %H:%M:%S")${NC}"
echo -e "${BLUE}File backup: ${BACKUP_FILE}${NC}"

# Esegui backup escludendo directory e file non necessari
echo -e "${GREEN}Creazione archivio...${NC}"

cd /workspace/db-ready

tar -czf "$BACKUP_PATH" \
    --exclude='vaadin-examples/target' \
    --exclude='vaadin-examples/node_modules' \
    --exclude='vaadin-examples/.git' \
    --exclude='vaadin-examples/.idea' \
    --exclude='vaadin-examples/.vscode' \
    --exclude='vaadin-examples/*.log' \
    --exclude='vaadin-examples/.DS_Store' \
    --exclude='vaadin-examples/frontend/generated' \
    --exclude='vaadin-examples/package-lock.json' \
    --exclude='vaadin-examples/pnpm-lock.yaml' \
    --exclude='vaadin-examples/.vaadin' \
    vaadin-examples/

# Verifica se il backup è stato creato
if [ -f "$BACKUP_PATH" ]; then
    # Ottieni dimensione file
    SIZE=$(du -h "$BACKUP_PATH" | cut -f1)
    echo -e "${GREEN}✅ Backup completato con successo!${NC}"
    echo -e "${GREEN}   File: $BACKUP_PATH${NC}"
    echo -e "${GREEN}   Dimensione: $SIZE${NC}"
    
    # Elenca i backup esistenti
    echo ""
    echo -e "${BLUE}Backup disponibili in $BACKUP_DIR:${NC}"
    ls -lh "$BACKUP_DIR"/*.tar.gz 2>/dev/null | awk '{print "   " $9 " (" $5 ")"}'
    
    # Conta numero di backup
    NUM_BACKUPS=$(ls -1 "$BACKUP_DIR"/*.tar.gz 2>/dev/null | wc -l)
    echo -e "${BLUE}Totale backup: $NUM_BACKUPS${NC}"
else
    echo -e "${RED}❌ Errore: Backup fallito!${NC}"
    exit 1
fi

echo -e "${BLUE}=== Backup completato ===${NC}"
