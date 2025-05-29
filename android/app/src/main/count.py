import os

def compter_lignes_java(dossier, exclusions=None):
    if exclusions is None:
        exclusions = {"build", "target", "out", ".git"}

    total_lignes = 0
    for racine, dossiers, fichiers in os.walk(dossier):
        # Exclure certains dossiers
        dossiers[:] = [d for d in dossiers if d not in exclusions and not d.startswith(".")]

        for fichier in fichiers:
            if fichier.endswith(".java"):
                chemin_fichier = os.path.join(racine, fichier)
                try:
                    with open(chemin_fichier, "r", encoding="utf-8") as f:
                        lignes = sum(1 for _ in f)
                        total_lignes += lignes
                        print(f"{chemin_fichier} : {lignes} lignes")
                except Exception as e:
                    print(f"Erreur avec {chemin_fichier} : {e}")

    print(f"\n✨ Total de lignes Java : {total_lignes} ✨")

if __name__ == "__main__":
    compter_lignes_java("./")
