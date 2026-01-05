SELECT d.id as dossier_id,  m.name AS medicament_count,p.id,p.type,p.pushed,pt.is_central
,pt.status AS status,t.id as treatment_id, p.id AS pid
FROM dossier d
JOIN prescription p ON p.dossier_id = d.id
JOIN patient_treatment pt ON pt.prescription_id = p.id
JOIN treatment t ON t.id = pt.treatment_id
JOIN medicament m ON m.id = t.medicament_id
WHERE d.numero = '25005091' order by treatment_id;
