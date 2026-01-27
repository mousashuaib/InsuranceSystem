--
-- PostgreSQL database dump
--

\restrict avRup4CkZYV20M4b0PuxUbGBmCItPRDQ4HICuYFBAeEIgp2CJNTcY7TP8YKAN51

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: annual_usage; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.annual_usage (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    service_type character varying(50) NOT NULL,
    total_amount numeric(12,2),
    total_count integer,
    updated_at timestamp(6) with time zone,
    year integer NOT NULL,
    client_id uuid NOT NULL
);


ALTER TABLE public.annual_usage OWNER TO postgres;

--
-- Name: chronic_patient_schedules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.chronic_patient_schedules (
    id uuid NOT NULL,
    amount double precision,
    created_at timestamp(6) with time zone NOT NULL,
    description text,
    interval_months integer NOT NULL,
    is_active boolean NOT NULL,
    lab_test_name character varying(200),
    last_sent_at timestamp(6) with time zone,
    medication_name character varying(200),
    medication_quantity integer,
    next_send_date date,
    notes text,
    radiology_test_name character varying(200),
    schedule_type character varying(20) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    patient_id uuid NOT NULL
);


ALTER TABLE public.chronic_patient_schedules OWNER TO postgres;

--
-- Name: claim_invoice_images; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.claim_invoice_images (
    claim_id uuid NOT NULL,
    image_path character varying(255)
);


ALTER TABLE public.claim_invoice_images OWNER TO postgres;

--
-- Name: claims; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.claims (
    id uuid NOT NULL,
    amount double precision NOT NULL,
    approved_at timestamp(6) with time zone,
    description character varying(255) NOT NULL,
    diagnosis character varying(255),
    doctor_name character varying(255),
    invoice_image_path character varying(255),
    provider_name character varying(255),
    rejected_at timestamp(6) with time zone,
    rejection_reason text,
    service_date date NOT NULL,
    status character varying(20) NOT NULL,
    submitted_at timestamp(6) with time zone,
    treatment_details character varying(255),
    member_id uuid NOT NULL,
    policy_id uuid NOT NULL,
    CONSTRAINT claims_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'APPROVED_BY_MEDICAL'::character varying, 'PENDING_MEDICAL'::character varying, 'PENDING_COORDINATION'::character varying, 'AWAITING_COORDINATION_REVIEW'::character varying, 'APPROVED_MEDICAL'::character varying, 'REJECTED_MEDICAL'::character varying, 'APPROVED_FINAL'::character varying, 'REJECTED_FINAL'::character varying, 'RETURNED_FOR_REVIEW'::character varying, 'RETURNED_TO_PROVIDER'::character varying, 'PAYMENT_PENDING'::character varying, 'PAID'::character varying])::text[])))
);


ALTER TABLE public.claims OWNER TO postgres;

--
-- Name: client_chronic_diseases; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_chronic_diseases (
    client_id uuid NOT NULL,
    disease character varying(255),
    CONSTRAINT client_chronic_diseases_disease_check CHECK (((disease)::text = ANY ((ARRAY['DIABETES'::character varying, 'HYPERTENSION'::character varying, 'ASTHMA'::character varying, 'HEART_DISEASE'::character varying, 'KIDNEY_DISEASE'::character varying, 'THYROID'::character varying, 'EPILEPSY'::character varying])::text[])))
);


ALTER TABLE public.client_chronic_diseases OWNER TO postgres;

--
-- Name: client_chronic_documents; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_chronic_documents (
    client_id uuid NOT NULL,
    document_path character varying(255)
);


ALTER TABLE public.client_chronic_documents OWNER TO postgres;

--
-- Name: client_doctor_documents; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_doctor_documents (
    client_id uuid NOT NULL,
    document_path character varying(255)
);


ALTER TABLE public.client_doctor_documents OWNER TO postgres;

--
-- Name: client_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_roles (
    client_id uuid NOT NULL,
    role_id uuid NOT NULL
);


ALTER TABLE public.client_roles OWNER TO postgres;

--
-- Name: client_university_cards; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.client_university_cards (
    client_id uuid NOT NULL,
    image_path character varying(255)
);


ALTER TABLE public.client_university_cards OWNER TO postgres;

--
-- Name: clients; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.clients (
    id uuid NOT NULL,
    clinic_location character varying(200),
    created_at timestamp(6) with time zone NOT NULL,
    date_of_birth date,
    department character varying(150),
    email character varying(150),
    email_verification_code character varying(10),
    email_verification_expiry timestamp(6) with time zone,
    email_verified boolean DEFAULT false NOT NULL,
    employee_id character varying(50),
    faculty character varying(150),
    full_name character varying(150) NOT NULL,
    gender character varying(10),
    lab_code character varying(50),
    lab_location character varying(200),
    lab_name character varying(150),
    national_id character varying(20),
    password_hash character varying(255) NOT NULL,
    pharmacy_code character varying(50),
    pharmacy_location character varying(200),
    pharmacy_name character varying(150),
    phone character varying(40),
    radiology_code character varying(50),
    radiology_location character varying(200),
    radiology_name character varying(150),
    requested_role character varying(40),
    role_request_status character varying(20) NOT NULL,
    specialization character varying(150),
    status character varying(20) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    policy_id uuid,
    CONSTRAINT clients_requested_role_check CHECK (((requested_role)::text = ANY ((ARRAY['INSURANCE_CLIENT'::character varying, 'DOCTOR'::character varying, 'PHARMACIST'::character varying, 'LAB_TECH'::character varying, 'INSURANCE_MANAGER'::character varying, 'RADIOLOGIST'::character varying, 'MEDICAL_ADMIN'::character varying, 'COORDINATION_ADMIN'::character varying])::text[]))),
    CONSTRAINT clients_role_request_status_check CHECK (((role_request_status)::text = ANY ((ARRAY['NONE'::character varying, 'PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[]))),
    CONSTRAINT clients_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'DEACTIVATED'::character varying])::text[])))
);


ALTER TABLE public.clients OWNER TO postgres;

--
-- Name: conversations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.conversations (
    id uuid NOT NULL,
    conversation_type character varying(50),
    last_updated timestamp(6) with time zone NOT NULL,
    user1_id uuid NOT NULL,
    user2_id uuid NOT NULL
);


ALTER TABLE public.conversations OWNER TO postgres;

--
-- Name: coverage_usage; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.coverage_usage (
    id uuid NOT NULL,
    amount_used numeric(12,2),
    created_at timestamp(6) with time zone,
    provider_specialization character varying(100),
    service_type character varying(50),
    usage_date date NOT NULL,
    visit_count integer,
    year integer NOT NULL,
    client_id uuid NOT NULL
);


ALTER TABLE public.coverage_usage OWNER TO postgres;

--
-- Name: coverages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.coverages (
    id uuid NOT NULL,
    allowed_gender character varying(255),
    amount numeric(12,2) NOT NULL,
    coverage_percent numeric(5,2) NOT NULL,
    coverage_type character varying(255) NOT NULL,
    is_covered boolean NOT NULL,
    description text,
    emergency_eligible boolean NOT NULL,
    frequency_limit integer,
    frequency_period character varying(255),
    max_age integer,
    max_limit numeric(12,2),
    min_age integer,
    minimum_deductible numeric(12,2),
    requires_referral boolean NOT NULL,
    service_name character varying(160) NOT NULL,
    policy_id uuid NOT NULL,
    CONSTRAINT coverages_allowed_gender_check CHECK (((allowed_gender)::text = ANY ((ARRAY['MALE'::character varying, 'FEMALE'::character varying, 'CHILD'::character varying, 'ALL'::character varying])::text[]))),
    CONSTRAINT coverages_coverage_type_check CHECK (((coverage_type)::text = ANY ((ARRAY['OUTPATIENT'::character varying, 'INPATIENT'::character varying, 'DENTAL'::character varying, 'OPTICAL'::character varying, 'EMERGENCY'::character varying, 'LAB'::character varying, 'XRAY'::character varying])::text[]))),
    CONSTRAINT coverages_frequency_period_check CHECK (((frequency_period)::text = ANY ((ARRAY['DAILY'::character varying, 'WEEKLY'::character varying, 'MONTHLY'::character varying, 'YEARLY'::character varying])::text[])))
);


ALTER TABLE public.coverages OWNER TO postgres;

--
-- Name: doctor_medicine_assignments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.doctor_medicine_assignments (
    id uuid NOT NULL,
    active boolean NOT NULL,
    assigned_at timestamp(6) with time zone NOT NULL,
    max_daily_prescriptions integer,
    max_quantity_per_prescription integer,
    notes text,
    specialization character varying(150),
    updated_at timestamp(6) with time zone,
    assigned_by uuid,
    doctor_id uuid NOT NULL,
    medicine_id uuid NOT NULL
);


ALTER TABLE public.doctor_medicine_assignments OWNER TO postgres;

--
-- Name: doctor_procedures; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.doctor_procedures (
    id uuid NOT NULL,
    active boolean NOT NULL,
    category character varying(255) NOT NULL,
    coverage_percentage integer,
    coverage_status character varying(255) NOT NULL,
    created_at timestamp(6) with time zone,
    max_price numeric(12,2),
    price numeric(12,2) NOT NULL,
    procedure_name character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone,
    CONSTRAINT doctor_procedures_coverage_status_check CHECK (((coverage_status)::text = ANY ((ARRAY['COVERED'::character varying, 'REQUIRES_APPROVAL'::character varying, 'NOT_COVERED'::character varying])::text[])))
);


ALTER TABLE public.doctor_procedures OWNER TO postgres;

--
-- Name: doctor_specialization; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.doctor_specialization (
    id bigint NOT NULL,
    consultation_price double precision NOT NULL,
    diagnoses text[],
    diagnosis_treatment_mappings text,
    display_name character varying(255) NOT NULL,
    gender_restriction character varying(255),
    max_age integer,
    min_age integer,
    treatment_plans text[]
);


ALTER TABLE public.doctor_specialization OWNER TO postgres;

--
-- Name: doctor_specialization_allowed_genders; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.doctor_specialization_allowed_genders (
    specialization_id bigint CONSTRAINT doctor_specialization_allowed_gender_specialization_id_not_null NOT NULL,
    gender character varying(255)
);


ALTER TABLE public.doctor_specialization_allowed_genders OWNER TO postgres;

--
-- Name: doctor_specialization_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.doctor_specialization ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.doctor_specialization_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: doctor_test_assignments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.doctor_test_assignments (
    id uuid NOT NULL,
    active boolean NOT NULL,
    assigned_at timestamp(6) with time zone NOT NULL,
    max_daily_requests integer,
    notes text,
    specialization character varying(150),
    test_type character varying(20) NOT NULL,
    updated_at timestamp(6) with time zone,
    assigned_by uuid,
    doctor_id uuid NOT NULL,
    test_id uuid NOT NULL
);


ALTER TABLE public.doctor_test_assignments OWNER TO postgres;

--
-- Name: emergency_requests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.emergency_requests (
    id uuid NOT NULL,
    approved_at timestamp(6) with time zone,
    contact_phone character varying(255) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    description text NOT NULL,
    doctor_id uuid NOT NULL,
    family_member_id uuid,
    incident_date date NOT NULL,
    location character varying(255) NOT NULL,
    notes text,
    rejected_at timestamp(6) with time zone,
    rejection_reason text,
    status character varying(255) NOT NULL,
    submitted_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone,
    member_id uuid NOT NULL,
    CONSTRAINT emergency_requests_status_check CHECK (((status)::text = ANY ((ARRAY['APPROVED_BY_MEDICAL'::character varying, 'REJECTED_BY_MEDICAL'::character varying, 'PENDING_MEDICAL'::character varying, 'PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.emergency_requests OWNER TO postgres;

--
-- Name: family_member_documents; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.family_member_documents (
    family_member_id uuid NOT NULL,
    document_path character varying(255)
);


ALTER TABLE public.family_member_documents OWNER TO postgres;

--
-- Name: family_members; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.family_members (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    date_of_birth date NOT NULL,
    full_name character varying(150) NOT NULL,
    gender character varying(10) NOT NULL,
    insurance_number character varying(30) NOT NULL,
    national_id character varying(20) NOT NULL,
    relation character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    client_id uuid NOT NULL,
    CONSTRAINT family_members_gender_check CHECK (((gender)::text = ANY ((ARRAY['MALE'::character varying, 'FEMALE'::character varying])::text[]))),
    CONSTRAINT family_members_relation_check CHECK (((relation)::text = ANY ((ARRAY['WIFE'::character varying, 'HUSBAND'::character varying, 'SON'::character varying, 'DAUGHTER'::character varying, 'FATHER'::character varying, 'MOTHER'::character varying])::text[]))),
    CONSTRAINT family_members_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.family_members OWNER TO postgres;

--
-- Name: healthcare_provider_claims; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.healthcare_provider_claims (
    id uuid NOT NULL,
    amount double precision NOT NULL,
    approved_at timestamp(6) with time zone,
    client_id uuid,
    client_name character varying(255),
    client_pay_amount numeric(12,2),
    coverage_message text,
    coverage_percent_used numeric(5,2),
    description text NOT NULL,
    diagnosis text,
    doctor_name character varying(255),
    emergency boolean,
    insurance_covered_amount numeric(12,2),
    invoice_image_path character varying(255),
    is_chronic boolean,
    is_covered boolean,
    is_follow_up boolean DEFAULT false,
    max_coverage_used numeric(12,2),
    medical_reviewed_at timestamp(6) with time zone,
    medical_reviewer_id uuid,
    medical_reviewer_name character varying(255),
    original_consultation_fee numeric(12,2),
    paid_at timestamp(6) with time zone,
    paid_by uuid,
    rejected_at timestamp(6) with time zone,
    rejection_reason text,
    role_specific_data text,
    service_date date NOT NULL,
    status character varying(30) NOT NULL,
    submitted_at timestamp(6) with time zone,
    treatment_details text,
    provider_id uuid NOT NULL,
    policy_id uuid,
    CONSTRAINT healthcare_provider_claims_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'APPROVED_BY_MEDICAL'::character varying, 'PENDING_MEDICAL'::character varying, 'PENDING_COORDINATION'::character varying, 'AWAITING_COORDINATION_REVIEW'::character varying, 'APPROVED_MEDICAL'::character varying, 'REJECTED_MEDICAL'::character varying, 'APPROVED_FINAL'::character varying, 'REJECTED_FINAL'::character varying, 'RETURNED_FOR_REVIEW'::character varying, 'RETURNED_TO_PROVIDER'::character varying, 'PAYMENT_PENDING'::character varying, 'PAID'::character varying])::text[])))
);


ALTER TABLE public.healthcare_provider_claims OWNER TO postgres;

--
-- Name: lab_requests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.lab_requests (
    id uuid NOT NULL,
    approved_price double precision,
    created_at timestamp(6) with time zone,
    diagnosis text,
    entered_price double precision,
    notes character varying(255),
    result_url character varying(255),
    status character varying(255),
    test_name character varying(255) NOT NULL,
    treatment text,
    updated_at timestamp(6) with time zone,
    doctor_id uuid NOT NULL,
    lab_tech_id uuid,
    member_id uuid NOT NULL,
    price_id uuid,
    CONSTRAINT lab_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.lab_requests OWNER TO postgres;

--
-- Name: medical_diagnoses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.medical_diagnoses (
    id uuid NOT NULL,
    active boolean NOT NULL,
    arabic_name character varying(255) NOT NULL,
    created_at timestamp(6) with time zone,
    description text,
    english_name character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone
);


ALTER TABLE public.medical_diagnoses OWNER TO postgres;

--
-- Name: medical_records; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.medical_records (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    diagnosis text,
    notes text,
    treatment text,
    updated_at timestamp(6) with time zone,
    doctor_id uuid NOT NULL,
    member_id uuid NOT NULL
);


ALTER TABLE public.medical_records OWNER TO postgres;

--
-- Name: medical_tests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.medical_tests (
    id uuid NOT NULL,
    active boolean NOT NULL,
    category character varying(255) NOT NULL,
    coverage_percentage integer,
    coverage_status character varying(255) NOT NULL,
    created_at timestamp(6) with time zone,
    price numeric(12,2),
    test_name character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone,
    CONSTRAINT medical_tests_coverage_status_check CHECK (((coverage_status)::text = ANY ((ARRAY['COVERED'::character varying, 'REQUIRES_APPROVAL'::character varying, 'NOT_COVERED'::character varying])::text[])))
);


ALTER TABLE public.medical_tests OWNER TO postgres;

--
-- Name: medicine_prices; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.medicine_prices (
    id uuid NOT NULL,
    active boolean NOT NULL,
    composition text,
    coverage_percentage integer,
    coverage_status character varying(255) NOT NULL,
    created_at timestamp(6) with time zone,
    drug_name character varying(255) NOT NULL,
    generic_name text,
    price numeric(12,2) NOT NULL,
    type character varying(255),
    unit character varying(255),
    updated_at timestamp(6) with time zone,
    CONSTRAINT medicine_prices_coverage_status_check CHECK (((coverage_status)::text = ANY ((ARRAY['COVERED'::character varying, 'REQUIRES_APPROVAL'::character varying, 'NOT_COVERED'::character varying])::text[])))
);


ALTER TABLE public.medicine_prices OWNER TO postgres;

--
-- Name: messages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.messages (
    id uuid NOT NULL,
    content character varying(2000) NOT NULL,
    is_read boolean NOT NULL,
    sent_at timestamp(6) with time zone NOT NULL,
    conversation_id uuid NOT NULL,
    receiver_id uuid NOT NULL,
    sender_id uuid NOT NULL
);


ALTER TABLE public.messages OWNER TO postgres;

--
-- Name: notifications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notifications (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    message character varying(500) NOT NULL,
    is_read boolean NOT NULL,
    replied boolean NOT NULL,
    type character varying(255),
    recipient_id uuid NOT NULL,
    sender_id uuid NOT NULL,
    CONSTRAINT notifications_type_check CHECK (((type)::text = ANY ((ARRAY['MANUAL_MESSAGE'::character varying, 'CLAIM'::character varying, 'EMERGENCY'::character varying, 'SYSTEM'::character varying])::text[])))
);


ALTER TABLE public.notifications OWNER TO postgres;

--
-- Name: password_reset_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.password_reset_tokens (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    token character varying(255) NOT NULL,
    used boolean NOT NULL,
    username character varying(255) NOT NULL
);


ALTER TABLE public.password_reset_tokens OWNER TO postgres;

--
-- Name: policies; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.policies (
    id uuid NOT NULL,
    coverage_limit numeric(12,2) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    deductible numeric(12,2) NOT NULL,
    description text,
    emergency_rules text,
    end_date date NOT NULL,
    name character varying(120) NOT NULL,
    policy_no character varying(50) NOT NULL,
    start_date date NOT NULL,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT policies_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'EXPIRED'::character varying])::text[])))
);


ALTER TABLE public.policies OWNER TO postgres;

--
-- Name: prescription_items; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.prescription_items (
    id uuid NOT NULL,
    calculated_quantity integer,
    covered_quantity integer,
    created_at timestamp(6) with time zone,
    dispensed_quantity integer,
    dosage integer,
    drug_form character varying(255),
    duration integer,
    expiry_date timestamp(6) with time zone,
    final_price double precision,
    pharmacist_price double precision,
    pharmacist_price_per_unit double precision,
    times_per_day integer,
    union_price_per_unit double precision,
    updated_at timestamp(6) with time zone,
    prescription_id uuid NOT NULL,
    price_list_id uuid
);


ALTER TABLE public.prescription_items OWNER TO postgres;

--
-- Name: prescriptions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.prescriptions (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    diagnosis text,
    is_chronic boolean,
    status character varying(255) NOT NULL,
    total_price double precision,
    treatment text,
    updated_at timestamp(6) with time zone,
    doctor_id uuid NOT NULL,
    member_id uuid NOT NULL,
    pharmacist_id uuid,
    CONSTRAINT prescriptions_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'VERIFIED'::character varying, 'REJECTED'::character varying, 'BILLED'::character varying])::text[])))
);


ALTER TABLE public.prescriptions OWNER TO postgres;

--
-- Name: price_list; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.price_list (
    id uuid NOT NULL,
    active boolean NOT NULL,
    coverage_percentage integer,
    coverage_status character varying(255) NOT NULL,
    created_at timestamp(6) with time zone,
    max_age integer,
    min_age integer,
    notes text,
    price double precision NOT NULL,
    provider_type character varying(255) NOT NULL,
    quantity integer,
    service_code character varying(255),
    service_details jsonb,
    service_name character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone,
    CONSTRAINT price_list_coverage_status_check CHECK (((coverage_status)::text = ANY ((ARRAY['COVERED'::character varying, 'REQUIRES_APPROVAL'::character varying, 'NOT_COVERED'::character varying])::text[]))),
    CONSTRAINT price_list_provider_type_check CHECK (((provider_type)::text = ANY ((ARRAY['PHARMACY'::character varying, 'LAB'::character varying, 'RADIOLOGY'::character varying, 'DOCTOR'::character varying])::text[])))
);


ALTER TABLE public.price_list OWNER TO postgres;

--
-- Name: price_list_allowed_genders; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.price_list_allowed_genders (
    price_list_id uuid NOT NULL,
    gender character varying(255)
);


ALTER TABLE public.price_list_allowed_genders OWNER TO postgres;

--
-- Name: price_list_allowed_specializations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.price_list_allowed_specializations (
    price_list_id uuid NOT NULL,
    specialization_id bigint NOT NULL
);


ALTER TABLE public.price_list_allowed_specializations OWNER TO postgres;

--
-- Name: provider_policies; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.provider_policies (
    id uuid NOT NULL,
    active boolean,
    coverage_percent numeric(5,2),
    created_at timestamp(6) with time zone,
    effective_from date,
    effective_to date,
    negotiated_price numeric(12,2),
    service_name character varying(160) NOT NULL,
    updated_at timestamp(6) with time zone,
    provider_id uuid NOT NULL
);


ALTER TABLE public.provider_policies OWNER TO postgres;

--
-- Name: radiology_requests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.radiology_requests (
    id uuid NOT NULL,
    approved_price double precision,
    created_at timestamp(6) with time zone,
    diagnosis text,
    entered_price double precision,
    notes character varying(255),
    result_url character varying(255),
    status character varying(255),
    test_name character varying(255),
    treatment text,
    updated_at timestamp(6) with time zone,
    doctor_id uuid NOT NULL,
    member_id uuid NOT NULL,
    radiologist_id uuid,
    price_id uuid,
    CONSTRAINT radiology_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.radiology_requests OWNER TO postgres;

--
-- Name: revoked_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.revoked_tokens (
    id bigint NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    revoked_at timestamp(6) with time zone NOT NULL,
    token character varying(500) NOT NULL
);


ALTER TABLE public.revoked_tokens OWNER TO postgres;

--
-- Name: revoked_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.revoked_tokens ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.revoked_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    id uuid NOT NULL,
    name character varying(40) NOT NULL,
    CONSTRAINT roles_name_check CHECK (((name)::text = ANY ((ARRAY['INSURANCE_CLIENT'::character varying, 'DOCTOR'::character varying, 'PHARMACIST'::character varying, 'LAB_TECH'::character varying, 'INSURANCE_MANAGER'::character varying, 'RADIOLOGIST'::character varying, 'MEDICAL_ADMIN'::character varying, 'COORDINATION_ADMIN'::character varying])::text[])))
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- Name: search_profiles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.search_profiles (
    id uuid NOT NULL,
    address character varying(255),
    clinic_registration character varying(300),
    contact_info character varying(100),
    description character varying(500),
    id_or_passport_copy character varying(300),
    location_lat double precision,
    location_lng double precision,
    medical_license character varying(300),
    name character varying(150) NOT NULL,
    rejection_reason character varying(300),
    status character varying(20) NOT NULL,
    type character varying(20) NOT NULL,
    university_degree character varying(300),
    owner_id uuid NOT NULL,
    CONSTRAINT search_profiles_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[]))),
    CONSTRAINT search_profiles_type_check CHECK (((type)::text = ANY ((ARRAY['CLINIC'::character varying, 'PHARMACY'::character varying, 'LAB'::character varying, 'DOCTOR'::character varying, 'RADIOLOGY'::character varying])::text[])))
);


ALTER TABLE public.search_profiles OWNER TO postgres;

--
-- Name: tests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tests (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    test_name character varying(255) NOT NULL,
    union_price double precision NOT NULL,
    updated_at timestamp(6) with time zone
);


ALTER TABLE public.tests OWNER TO postgres;

--
-- Name: v_role_id; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.v_role_id (
    id uuid
);


ALTER TABLE public.v_role_id OWNER TO postgres;

--
-- Name: visits; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.visits (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    doctor_specialization character varying(150) NOT NULL,
    notes text,
    updated_at timestamp(6) with time zone NOT NULL,
    visit_date date NOT NULL,
    visit_type character varying(20) NOT NULL,
    visit_year integer NOT NULL,
    doctor_id uuid NOT NULL,
    family_member_id uuid,
    patient_id uuid,
    previous_visit_id uuid,
    CONSTRAINT visits_visit_type_check CHECK (((visit_type)::text = ANY ((ARRAY['NORMAL'::character varying, 'FOLLOW_UP'::character varying])::text[])))
);


ALTER TABLE public.visits OWNER TO postgres;

--
-- Data for Name: annual_usage; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.annual_usage (id, created_at, service_type, total_amount, total_count, updated_at, year, client_id) FROM stdin;
\.


--
-- Data for Name: chronic_patient_schedules; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.chronic_patient_schedules (id, amount, created_at, description, interval_months, is_active, lab_test_name, last_sent_at, medication_name, medication_quantity, next_send_date, notes, radiology_test_name, schedule_type, updated_at, patient_id) FROM stdin;
\.


--
-- Data for Name: claim_invoice_images; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.claim_invoice_images (claim_id, image_path) FROM stdin;
\.


--
-- Data for Name: claims; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.claims (id, amount, approved_at, description, diagnosis, doctor_name, invoice_image_path, provider_name, rejected_at, rejection_reason, service_date, status, submitted_at, treatment_details, member_id, policy_id) FROM stdin;
\.


--
-- Data for Name: client_chronic_diseases; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_chronic_diseases (client_id, disease) FROM stdin;
20f27a3d-4257-438d-a327-ae327c0b29ce	HYPERTENSION
20f27a3d-4257-438d-a327-ae327c0b29ce	DIABETES
10da5d69-b91c-454b-b084-1c772dac7fa8	ASTHMA
f1d42ef1-3c2d-400f-b6d9-996309ec35d8	HEART_DISEASE
f1d42ef1-3c2d-400f-b6d9-996309ec35d8	THYROID
6a2df59a-141b-4fc7-85f4-b71195df9fb8	KIDNEY_DISEASE
\.


--
-- Data for Name: client_chronic_documents; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_chronic_documents (client_id, document_path) FROM stdin;
\.


--
-- Data for Name: client_doctor_documents; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_doctor_documents (client_id, document_path) FROM stdin;
342d358d-80a9-423e-8d45-1983a89cf3c9	/uploads/doctor/83ea5763-64a8-42b3-b356-b89215fc1b51.png
\.


--
-- Data for Name: client_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_roles (client_id, role_id) FROM stdin;
619463cd-6861-4f31-aa6e-eca589183af5	18abc6db-1fd4-4180-85aa-cf7573a0b77c
5c5b1b6c-ca8a-4083-a3d9-8f40022a5cd5	2e4f3359-f4ba-45a1-9e8a-2bbfb0986db5
65eb2e15-5a21-4f41-a21d-c12bb04a775b	f919990c-2eaa-41f1-8a9c-17c372c88ed6
95557a6b-92c4-4913-a9c0-8e3c35c67aad	90f4ee9f-bdbd-4cf8-8934-68a37e5e9011
2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	90f4ee9f-bdbd-4cf8-8934-68a37e5e9011
65ae7b21-d2bd-4685-a10c-76d406884d62	90f4ee9f-bdbd-4cf8-8934-68a37e5e9011
434a147e-eaed-46fd-931b-d0ebaf4bcaea	90f4ee9f-bdbd-4cf8-8934-68a37e5e9011
8c9de48c-10af-4670-aadb-9f9ebb9e75a1	90f4ee9f-bdbd-4cf8-8934-68a37e5e9011
1c49d82a-0f7d-4b53-801e-74761df1008f	90f4ee9f-bdbd-4cf8-8934-68a37e5e9011
c3ae6b3b-c835-448c-b0ad-298bec2c5cb3	90f4ee9f-bdbd-4cf8-8934-68a37e5e9011
ae9f721f-4e97-4312-a680-daf79eaeba1f	90f4ee9f-bdbd-4cf8-8934-68a37e5e9011
584a36c8-794c-42c6-9c09-e768dc32ee30	f7450117-cb68-4538-a649-e461e5e48448
01f4d329-987d-4560-98bf-298a3ece3f27	f7450117-cb68-4538-a649-e461e5e48448
8f491018-1a64-4a6f-b9d6-c19e2f2e7c7c	f7450117-cb68-4538-a649-e461e5e48448
4f789b0d-9385-485e-b9c7-a586af5cabd1	6c39d263-9044-4676-a8fb-a07569fd845d
0760c6f6-4000-4aba-8357-86ef3e8f392a	6c39d263-9044-4676-a8fb-a07569fd845d
b8b6de49-c402-4c8b-8320-5527e573992b	6c39d263-9044-4676-a8fb-a07569fd845d
48238880-3a07-4d63-8c1f-e7f5fc54da3b	62cde6f4-1869-42a4-9dd6-c386eb4d49c3
d2b04366-b548-40a8-957a-1adce0b9173e	62cde6f4-1869-42a4-9dd6-c386eb4d49c3
20f27a3d-4257-438d-a327-ae327c0b29ce	787d7aa2-a87c-4802-ab2c-0886f033c0b5
10da5d69-b91c-454b-b084-1c772dac7fa8	787d7aa2-a87c-4802-ab2c-0886f033c0b5
f1d42ef1-3c2d-400f-b6d9-996309ec35d8	787d7aa2-a87c-4802-ab2c-0886f033c0b5
48993e40-0bf9-4412-9b0b-9817cf6b920f	787d7aa2-a87c-4802-ab2c-0886f033c0b5
6a2df59a-141b-4fc7-85f4-b71195df9fb8	787d7aa2-a87c-4802-ab2c-0886f033c0b5
8508189c-6f4f-482a-9d99-433ba0270084	787d7aa2-a87c-4802-ab2c-0886f033c0b5
\.


--
-- Data for Name: client_university_cards; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.client_university_cards (client_id, image_path) FROM stdin;
\.


--
-- Data for Name: clients; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.clients (id, clinic_location, created_at, date_of_birth, department, email, email_verification_code, email_verification_expiry, email_verified, employee_id, faculty, full_name, gender, lab_code, lab_location, lab_name, national_id, password_hash, pharmacy_code, pharmacy_location, pharmacy_name, phone, radiology_code, radiology_location, radiology_name, requested_role, role_request_status, specialization, status, updated_at, policy_id) FROM stdin;
619463cd-6861-4f31-aa6e-eca589183af5	\N	2026-01-27 01:22:14.182206+02	1980-05-15	\N	manager@demo.com	\N	\N	t	EMP1000000001	\N	أحمد محمد المدير	M	\N	\N	\N	1000000001	$2a$10$amTV50ErQR2d4wlgmtnwKu6eC4cQUBCUwFQDU7sAh8YyUqde.3jLi	\N	\N	\N	0591000001	\N	\N	\N	INSURANCE_MANAGER	APPROVED	\N	ACTIVE	2026-01-27 01:22:14.182206+02	11aea7b1-5ee0-43a0-a746-52006c165769
5c5b1b6c-ca8a-4083-a3d9-8f40022a5cd5	\N	2026-01-27 01:22:14.279921+02	1985-08-20	\N	medical@demo.com	\N	\N	t	EMP1000000002	\N	سارة أحمد الطبية	F	\N	\N	\N	1000000002	$2a$10$wvvZdob7d1GCMJKQoGJ1UO6JbQTyoHslrecjVSWDMh.jCsZZhNK66	\N	\N	\N	0591000002	\N	\N	\N	MEDICAL_ADMIN	APPROVED	\N	ACTIVE	2026-01-27 01:22:14.279921+02	11aea7b1-5ee0-43a0-a746-52006c165769
65eb2e15-5a21-4f41-a21d-c12bb04a775b	\N	2026-01-27 01:22:14.370897+02	1982-03-10	\N	coordination@demo.com	\N	\N	t	EMP1000000003	\N	خالد عمر التنسيق	M	\N	\N	\N	1000000003	$2a$10$LwY92B98pXth9Kcdy24oV.LZgTOiNQvY439dHbAvXQqltv2nhZsJ6	\N	\N	\N	0591000003	\N	\N	\N	COORDINATION_ADMIN	APPROVED	\N	ACTIVE	2026-01-27 01:22:14.370897+02	11aea7b1-5ee0-43a0-a746-52006c165769
95557a6b-92c4-4913-a9c0-8e3c35c67aad	\N	2026-01-27 01:22:14.466435+02	1975-06-12	قسم أمراض القلب	doctor.cardio@demo.com	\N	\N	t	EMP2000000001	الطب	د. محمد أحمد القلب	M	\N	\N	\N	2000000001	$2a$10$gioHEUp.0UngznRgxpUnSuTAX9.DbFeTjLRbga7tuxR/Ez5WxDu1W	\N	\N	\N	0592000001	\N	\N	\N	DOCTOR	APPROVED	طب القلب	ACTIVE	2026-01-27 01:22:14.466435+02	11aea7b1-5ee0-43a0-a746-52006c165769
2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	\N	2026-01-27 01:22:14.568574+02	1980-09-25	قسم طب الأطفال	doctor.pediatric@demo.com	\N	\N	t	EMP2000000002	الطب	د. فاطمة علي الأطفال	F	\N	\N	\N	2000000002	$2a$10$oS4hb6jRmC6EXYocWWFIiOfLPzmxe9KuEUU7l5Qx7Wi8AqJyvXiJC	\N	\N	\N	0592000002	\N	\N	\N	DOCTOR	APPROVED	طب الأطفال	ACTIVE	2026-01-27 01:22:14.568574+02	11aea7b1-5ee0-43a0-a746-52006c165769
65ae7b21-d2bd-4685-a10c-76d406884d62	\N	2026-01-27 01:22:14.680837+02	1978-02-18	قسم الباطنة	doctor.internal@demo.com	\N	\N	t	EMP2000000003	الطب	د. عمر خالد الباطنة	M	\N	\N	\N	2000000003	$2a$10$MunFxrIMJ..ly6PO5CnOeeDZZ1fBZXBhWwbo7q74hBsybL4qqljUi	\N	\N	\N	0592000003	\N	\N	\N	DOCTOR	APPROVED	طب الباطنة	ACTIVE	2026-01-27 01:22:14.680837+02	11aea7b1-5ee0-43a0-a746-52006c165769
434a147e-eaed-46fd-931b-d0ebaf4bcaea	\N	2026-01-27 01:22:14.774+02	1982-11-05	قسم العيون	doctor.eye@demo.com	\N	\N	t	EMP2000000004	الطب	د. نور حسن العيون	F	\N	\N	\N	2000000004	$2a$10$znwo3yqdKM4Q6h/MAeZew.9CVocpv6UNstBWEF.ViecgOKoaYXIsG	\N	\N	\N	0592000004	\N	\N	\N	DOCTOR	APPROVED	طب العيون	ACTIVE	2026-01-27 01:22:14.774+02	11aea7b1-5ee0-43a0-a746-52006c165769
8c9de48c-10af-4670-aadb-9f9ebb9e75a1	\N	2026-01-27 01:22:14.865713+02	1985-04-30	قسم الأسنان	doctor.dental@demo.com	\N	\N	t	EMP2000000005	طب الأسنان	د. ياسر محمود الأسنان	M	\N	\N	\N	2000000005	$2a$10$U3lBCB5irIz.ZxhABNGVze1ne8HVtFNNWNJi/D3Px1lls1dj7CYva	\N	\N	\N	0592000005	\N	\N	\N	DOCTOR	APPROVED	طب الأسنان	ACTIVE	2026-01-27 01:22:14.865713+02	11aea7b1-5ee0-43a0-a746-52006c165769
1c49d82a-0f7d-4b53-801e-74761df1008f	\N	2026-01-27 01:22:14.961688+02	1979-07-15	قسم النساء والتوليد	doctor.gyn@demo.com	\N	\N	t	EMP2000000006	الطب	د. ريم سعيد النسائية	F	\N	\N	\N	2000000006	$2a$10$XGydRimrNKbJ6nbBBDO2beYyhp/QnGe8Ir55oyjKfpfwjPsFS.v.6	\N	\N	\N	0592000006	\N	\N	\N	DOCTOR	APPROVED	طب النساء والتوليد	ACTIVE	2026-01-27 01:22:14.961688+02	11aea7b1-5ee0-43a0-a746-52006c165769
c3ae6b3b-c835-448c-b0ad-298bec2c5cb3	\N	2026-01-27 01:22:15.050773+02	1976-12-08	قسم جراحة العظام	doctor.ortho@demo.com	\N	\N	t	EMP2000000007	الطب	د. سامي عادل العظام	M	\N	\N	\N	2000000007	$2a$10$HAwTOwi/4b14qJsxFfIniuBUEx.nXJRyDepygOIM8sDONNJBJU3OK	\N	\N	\N	0592000007	\N	\N	\N	DOCTOR	APPROVED	طب العظام	ACTIVE	2026-01-27 01:22:15.050773+02	11aea7b1-5ee0-43a0-a746-52006c165769
ae9f721f-4e97-4312-a680-daf79eaeba1f	\N	2026-01-27 01:22:15.145632+02	1983-01-22	قسم الجلدية	doctor.derma@demo.com	\N	\N	t	EMP2000000008	الطب	د. لينا كمال الجلدية	F	\N	\N	\N	2000000008	$2a$10$oBIpEFwrPGS0WZOkoGsym.kncbxR.atV4KwjZdHlOq3PrLIG1mktu	\N	\N	\N	0592000008	\N	\N	\N	DOCTOR	APPROVED	طب الجلدية	ACTIVE	2026-01-27 01:22:15.145632+02	11aea7b1-5ee0-43a0-a746-52006c165769
584a36c8-794c-42c6-9c09-e768dc32ee30	\N	2026-01-27 01:22:15.284493+02	1988-05-10	\N	pharmacy1@demo.com	\N	\N	t	EMP3000000001	\N	يوسف أحمد الصيدلي	M	\N	\N	\N	3000000001	$2a$10$hNYxim7jYMIp6OjbMcS/G.mjG2dtIlsUp/TCisWnT56B6w0.Ls0Nq	PH001	رام الله - شارع الإرسال	صيدلية الشفاء	0593000001	\N	\N	\N	PHARMACIST	APPROVED	\N	ACTIVE	2026-01-27 01:22:15.284493+02	11aea7b1-5ee0-43a0-a746-52006c165769
01f4d329-987d-4560-98bf-298a3ece3f27	\N	2026-01-27 01:22:15.379858+02	1990-08-15	\N	pharmacy2@demo.com	\N	\N	t	EMP3000000002	\N	هدى محمد الصيدلانية	F	\N	\N	\N	3000000002	$2a$10$byiT/7vbbyqa7KOyb5t/peZcjMnfsmrzkPc2fd5YdHPG282Cw9AHW	PH002	نابلس - شارع فيصل	صيدلية الأمل	0593000002	\N	\N	\N	PHARMACIST	APPROVED	\N	ACTIVE	2026-01-27 01:22:15.379858+02	11aea7b1-5ee0-43a0-a746-52006c165769
8f491018-1a64-4a6f-b9d6-c19e2f2e7c7c	\N	2026-01-27 01:22:15.468636+02	1986-03-20	\N	pharmacy3@demo.com	\N	\N	t	EMP3000000003	\N	كريم علي الصيدلي	M	\N	\N	\N	3000000003	$2a$10$MJQUqb8y2UBgTebwCJmByuk74bh8qz7GmPQfE9oqa.WP54YyHmIgi	PH003	الخليل - باب الزاوية	صيدلية النور	0593000003	\N	\N	\N	PHARMACIST	APPROVED	\N	ACTIVE	2026-01-27 01:22:15.468636+02	11aea7b1-5ee0-43a0-a746-52006c165769
4f789b0d-9385-485e-b9c7-a586af5cabd1	\N	2026-01-27 01:22:15.552482+02	1987-06-05	\N	lab1@demo.com	\N	\N	t	EMP4000000001	\N	عامر سعيد المختبر	M	LAB001	رام الله - المصيون	مختبر الحياة	4000000001	$2a$10$RRB8UzsyIfwE4cswbW5Wuuo5JM314dRn.omGzUM7N7iRd/PGIoGZ.	\N	\N	\N	0594000001	\N	\N	\N	LAB_TECH	APPROVED	\N	ACTIVE	2026-01-27 01:22:15.552482+02	11aea7b1-5ee0-43a0-a746-52006c165769
0760c6f6-4000-4aba-8357-86ef3e8f392a	\N	2026-01-27 01:22:15.643214+02	1991-09-12	\N	lab2@demo.com	\N	\N	t	EMP4000000002	\N	سمر خالد المختبر	F	LAB002	نابلس - دوار الشهداء	مختبر الأمل	4000000002	$2a$10$qQjjvb39SgABHkXJ5r.ByObRtrZ9seFD4q5gepJ3ptYvPTTnuDFxS	\N	\N	\N	0594000002	\N	\N	\N	LAB_TECH	APPROVED	\N	ACTIVE	2026-01-27 01:22:15.643214+02	11aea7b1-5ee0-43a0-a746-52006c165769
b8b6de49-c402-4c8b-8320-5527e573992b	\N	2026-01-27 01:22:15.73243+02	1985-04-18	\N	lab3@demo.com	\N	\N	t	EMP4000000003	\N	باسم عمر المختبر	M	LAB003	بيت لحم - شارع النجمة	مختبر الصحة	4000000003	$2a$10$T/wDiPuhLI/evD32pYr5SuP57qEzr.LNt1jIny5pMyMHVuk1Dy7UO	\N	\N	\N	0594000003	\N	\N	\N	LAB_TECH	APPROVED	\N	ACTIVE	2026-01-27 01:22:15.73243+02	11aea7b1-5ee0-43a0-a746-52006c165769
48238880-3a07-4d63-8c1f-e7f5fc54da3b	\N	2026-01-27 01:22:15.822665+02	1984-07-22	\N	radiology1@demo.com	\N	\N	t	EMP5000000001	\N	طارق حسين الأشعة	M	\N	\N	\N	5000000001	$2a$10$zVWNJM6urB.aWh4/zpx1...nAxDfAg4AJ2Zqb2T/LUpKloUMbmJlm	\N	\N	\N	0595000001	RAD001	رام الله - الماصيون	مركز الأشعة الحديث	RADIOLOGIST	APPROVED	\N	ACTIVE	2026-01-27 01:22:15.822665+02	11aea7b1-5ee0-43a0-a746-52006c165769
d2b04366-b548-40a8-957a-1adce0b9173e	\N	2026-01-27 01:22:15.915654+02	1989-11-08	\N	radiology2@demo.com	\N	\N	t	EMP5000000002	\N	منى أحمد الأشعة	F	\N	\N	\N	5000000002	$2a$10$Wa39CK3SP0XWxJaU0UkxOexvWdsVfVQ5A1InRyINxa/Krrhq4NJg2	\N	\N	\N	0595000002	RAD002	نابلس - المخفية	مركز التصوير الطبي	RADIOLOGIST	APPROVED	\N	ACTIVE	2026-01-27 01:22:15.915654+02	11aea7b1-5ee0-43a0-a746-52006c165769
20f27a3d-4257-438d-a327-ae327c0b29ce	\N	2026-01-27 01:22:16.003795+02	1985-03-15	\N	client1@demo.com	\N	\N	t	EMP6000000001	\N	محمد أحمد عبدالله	M	\N	\N	\N	6000000001	$2a$10$XFWLgRgCeD33wGi7gwEj3eEmolwSsRA9Z86U8O7XtG9/UKIcCqYxe	\N	\N	\N	0596000001	\N	\N	\N	INSURANCE_CLIENT	APPROVED	\N	ACTIVE	2026-01-27 01:22:16.003795+02	11aea7b1-5ee0-43a0-a746-52006c165769
10da5d69-b91c-454b-b084-1c772dac7fa8	\N	2026-01-27 01:22:16.106448+02	1990-07-20	\N	client2@demo.com	\N	\N	t	EMP6000000002	\N	فاطمة محمد سعيد	F	\N	\N	\N	6000000002	$2a$10$xD6f.jRbFHPt6E0.sQaFcOZi/6NaThta35CpGM51lMFCQZ1IdhpFy	\N	\N	\N	0596000002	\N	\N	\N	INSURANCE_CLIENT	APPROVED	\N	ACTIVE	2026-01-27 01:22:16.106448+02	11aea7b1-5ee0-43a0-a746-52006c165769
f1d42ef1-3c2d-400f-b6d9-996309ec35d8	\N	2026-01-27 01:22:16.19204+02	1982-09-10	\N	client3@demo.com	\N	\N	t	EMP6000000003	\N	أحمد خالد عمر	M	\N	\N	\N	6000000003	$2a$10$ltHVUql1yp5pJQP/utblh./6JFUawa4rwnZ/G1KUnuvEeUQ9JkEsG	\N	\N	\N	0596000003	\N	\N	\N	INSURANCE_CLIENT	APPROVED	\N	ACTIVE	2026-01-27 01:22:16.19204+02	9d7e59f0-6801-41a4-905a-23bf1ab663e1
48993e40-0bf9-4412-9b0b-9817cf6b920f	\N	2026-01-27 01:22:16.282445+02	1988-12-05	\N	client4@demo.com	\N	\N	t	EMP6000000004	\N	نور حسن علي	F	\N	\N	\N	6000000004	$2a$10$Ao..jwxVwXxo2IvofgsgTuaX/SNpCc0fxg7/LQpxKASpKFFtgUVVK	\N	\N	\N	0596000004	\N	\N	\N	INSURANCE_CLIENT	APPROVED	\N	ACTIVE	2026-01-27 01:22:16.282445+02	9d7e59f0-6801-41a4-905a-23bf1ab663e1
6a2df59a-141b-4fc7-85f4-b71195df9fb8	\N	2026-01-27 01:22:16.373707+02	1995-05-25	\N	client5@demo.com	\N	\N	t	EMP6000000005	\N	عمر سعيد محمد	M	\N	\N	\N	6000000005	$2a$10$vfyaTIHB7uvmgJWYpjU.HOp70sAeeRI3Nr4yg3373PlvYXmpYXiNy	\N	\N	\N	0596000005	\N	\N	\N	INSURANCE_CLIENT	APPROVED	\N	ACTIVE	2026-01-27 01:22:16.373707+02	6322e814-3596-41a9-9943-a31db4b2813e
8508189c-6f4f-482a-9d99-433ba0270084	\N	2026-01-27 01:22:16.459074+02	1992-02-14	\N	client6@demo.com	\N	\N	t	EMP6000000006	\N	سارة أحمد خالد	F	\N	\N	\N	6000000006	$2a$10$qSGLe1HgwsSwtjA4ghZD6ukFDHTIcMOL8fqObUj9VfC3mnWYtmPSm	\N	\N	\N	0596000006	\N	\N	\N	INSURANCE_CLIENT	APPROVED	\N	ACTIVE	2026-01-27 01:22:16.459074+02	6322e814-3596-41a9-9943-a31db4b2813e
342d358d-80a9-423e-8d45-1983a89cf3c9	ra	2026-01-27 05:27:35.000696+02	2003-02-25	\N	example@example.example	\N	\N	t	\N	\N	جاد جاد جاد	MALE	\N	\N	\N	0123456789	$2a$10$pQ/KbqIS1wSrF34sHULFu.Bz390oJF6h7FaIZRYnlrxkZKfVzYuS2	\N	\N	\N	0594961763	\N	\N	\N	DOCTOR	PENDING	طب النساء والتوليد	INACTIVE	2026-01-27 05:27:35.01278+02	\N
\.


--
-- Data for Name: conversations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.conversations (id, conversation_type, last_updated, user1_id, user2_id) FROM stdin;
\.


--
-- Data for Name: coverage_usage; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.coverage_usage (id, amount_used, created_at, provider_specialization, service_type, usage_date, visit_count, year, client_id) FROM stdin;
\.


--
-- Data for Name: coverages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.coverages (id, allowed_gender, amount, coverage_percent, coverage_type, is_covered, description, emergency_eligible, frequency_limit, frequency_period, max_age, max_limit, min_age, minimum_deductible, requires_referral, service_name, policy_id) FROM stdin;
7d4284b6-e215-44b9-b2f4-bc6d518b1b02	ALL	0.00	100.00	OUTPATIENT	t	تغطية كشف طبي عام	t	\N	\N	\N	500.00	\N	10.00	f	كشف طبي عام	11aea7b1-5ee0-43a0-a746-52006c165769
e35dae59-c489-445c-9a2c-ee98dad1be8b	ALL	0.00	100.00	OUTPATIENT	t	تغطية كشف طبي متخصص	t	\N	\N	\N	1000.00	\N	10.00	f	كشف طبي متخصص	11aea7b1-5ee0-43a0-a746-52006c165769
7e71af76-2508-4f09-ac8f-7227507b60bc	ALL	0.00	100.00	INPATIENT	t	تغطية إقامة في المستشفى	t	\N	\N	\N	50000.00	\N	10.00	f	إقامة في المستشفى	11aea7b1-5ee0-43a0-a746-52006c165769
c73efc5e-458c-4667-93ca-7366013e96c2	ALL	0.00	90.00	INPATIENT	t	تغطية عمليات جراحية	t	\N	\N	\N	30000.00	\N	10.00	f	عمليات جراحية	11aea7b1-5ee0-43a0-a746-52006c165769
adcd7b37-a7ab-43d9-8007-c7c65642934f	ALL	0.00	80.00	DENTAL	t	تغطية علاج الأسنان	f	\N	\N	\N	5000.00	\N	10.00	f	علاج الأسنان	11aea7b1-5ee0-43a0-a746-52006c165769
d183311e-9bc8-404f-8ac5-45c9316d7d80	ALL	0.00	80.00	OPTICAL	t	تغطية فحوصات النظر والنظارات	f	\N	\N	\N	3000.00	\N	10.00	f	فحوصات النظر والنظارات	11aea7b1-5ee0-43a0-a746-52006c165769
7e4d5ed0-fff2-477d-94c3-881f5c2c6897	ALL	0.00	100.00	EMERGENCY	t	تغطية طوارئ وإسعاف	t	\N	\N	\N	20000.00	\N	10.00	f	طوارئ وإسعاف	11aea7b1-5ee0-43a0-a746-52006c165769
dfb18585-64e2-4387-8430-d42b2abc40dc	ALL	0.00	100.00	LAB	t	تغطية فحوصات مخبرية	f	\N	\N	\N	10000.00	\N	10.00	f	فحوصات مخبرية	11aea7b1-5ee0-43a0-a746-52006c165769
123740cd-324a-4d34-8222-c5ba7ee3b0c4	ALL	0.00	100.00	XRAY	t	تغطية أشعة وتصوير	f	\N	\N	\N	15000.00	\N	10.00	f	أشعة وتصوير	11aea7b1-5ee0-43a0-a746-52006c165769
fcad7fa6-3d35-45da-835b-5934d1f46c86	ALL	0.00	80.00	OUTPATIENT	t	تغطية كشف طبي عام	t	\N	\N	\N	300.00	\N	10.00	f	كشف طبي عام	9d7e59f0-6801-41a4-905a-23bf1ab663e1
c522a02e-bf12-4d8e-a79a-cd5d7d73f6df	ALL	0.00	70.00	OUTPATIENT	t	تغطية كشف طبي متخصص	t	\N	\N	\N	500.00	\N	10.00	f	كشف طبي متخصص	9d7e59f0-6801-41a4-905a-23bf1ab663e1
d2636385-7bf7-42e5-98a9-863b032cdc52	ALL	0.00	80.00	INPATIENT	t	تغطية إقامة في المستشفى	t	\N	\N	\N	30000.00	\N	10.00	f	إقامة في المستشفى	9d7e59f0-6801-41a4-905a-23bf1ab663e1
42189c78-8870-4b00-b17b-6df95223623f	ALL	0.00	70.00	INPATIENT	t	تغطية عمليات جراحية	t	\N	\N	\N	20000.00	\N	10.00	f	عمليات جراحية	9d7e59f0-6801-41a4-905a-23bf1ab663e1
2a6d20ca-5be9-463d-b411-8805157323be	ALL	0.00	90.00	EMERGENCY	t	تغطية طوارئ وإسعاف	t	\N	\N	\N	15000.00	\N	10.00	f	طوارئ وإسعاف	9d7e59f0-6801-41a4-905a-23bf1ab663e1
8bcab8c4-24ed-4b1e-b370-ce58b69803db	ALL	0.00	80.00	LAB	t	تغطية فحوصات مخبرية	f	\N	\N	\N	5000.00	\N	10.00	f	فحوصات مخبرية	9d7e59f0-6801-41a4-905a-23bf1ab663e1
1924455f-2627-44b6-92a3-34ff2f6a46c1	ALL	0.00	80.00	XRAY	t	تغطية أشعة وتصوير	f	\N	\N	\N	8000.00	\N	10.00	f	أشعة وتصوير	9d7e59f0-6801-41a4-905a-23bf1ab663e1
41c3fbd1-3b0e-46cd-bbe7-0321d4a3524b	ALL	0.00	60.00	OUTPATIENT	t	تغطية كشف طبي عام	t	\N	\N	\N	200.00	\N	10.00	f	كشف طبي عام	6322e814-3596-41a9-9943-a31db4b2813e
935dc534-71a4-4a17-9759-09715500d7b6	ALL	0.00	60.00	INPATIENT	t	تغطية إقامة في المستشفى	t	\N	\N	\N	15000.00	\N	10.00	f	إقامة في المستشفى	6322e814-3596-41a9-9943-a31db4b2813e
abda5b23-ad19-4bbd-8c2a-5ff266dc4a87	ALL	0.00	80.00	EMERGENCY	t	تغطية طوارئ وإسعاف	t	\N	\N	\N	10000.00	\N	10.00	f	طوارئ وإسعاف	6322e814-3596-41a9-9943-a31db4b2813e
71120cdd-e2d0-4298-8b1c-32676c1835d2	ALL	0.00	60.00	LAB	t	تغطية فحوصات مخبرية	f	\N	\N	\N	2000.00	\N	10.00	f	فحوصات مخبرية	6322e814-3596-41a9-9943-a31db4b2813e
0919a3a1-a6b2-409f-8a11-0ab4e8c6c09f	ALL	0.00	60.00	XRAY	t	تغطية أشعة وتصوير	f	\N	\N	\N	3000.00	\N	10.00	f	أشعة وتصوير	6322e814-3596-41a9-9943-a31db4b2813e
\.


--
-- Data for Name: doctor_medicine_assignments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.doctor_medicine_assignments (id, active, assigned_at, max_daily_prescriptions, max_quantity_per_prescription, notes, specialization, updated_at, assigned_by, doctor_id, medicine_id) FROM stdin;
66b58e33-5acc-4caa-a0e9-e3cc604d0b7c	t	2026-01-27 01:22:18.131776+02	20	90	Auto-assigned during system initialization	طب القلب	2026-01-27 01:22:18.131776+02	619463cd-6861-4f31-aa6e-eca589183af5	95557a6b-92c4-4913-a9c0-8e3c35c67aad	c1ea6c9f-5b3b-4e33-a849-5a131ba686a7
f3009e43-3239-4f15-a2d1-f8021cd24026	t	2026-01-27 01:22:18.131776+02	20	90	Auto-assigned during system initialization	طب القلب	2026-01-27 01:22:18.131776+02	619463cd-6861-4f31-aa6e-eca589183af5	95557a6b-92c4-4913-a9c0-8e3c35c67aad	f8fe3448-4f39-42b8-95b5-fb0fb1a2e2cd
1095cc74-c570-4a22-8314-df35af36415c	t	2026-01-27 01:22:18.131776+02	20	90	Auto-assigned during system initialization	طب القلب	2026-01-27 01:22:18.131776+02	619463cd-6861-4f31-aa6e-eca589183af5	95557a6b-92c4-4913-a9c0-8e3c35c67aad	ec1725b9-24b5-4c2d-acb0-8b994f68187b
694c5353-d7d7-49e9-87ee-c30b136c54cd	t	2026-01-27 01:22:18.132774+02	20	90	Auto-assigned during system initialization	طب القلب	2026-01-27 01:22:18.132774+02	619463cd-6861-4f31-aa6e-eca589183af5	95557a6b-92c4-4913-a9c0-8e3c35c67aad	6ad30801-7df2-4d58-b5f9-45c59cf229fc
92a2acd6-62b3-4a15-918e-b47b6970aff3	t	2026-01-27 01:22:18.132774+02	20	90	Auto-assigned during system initialization	طب القلب	2026-01-27 01:22:18.132774+02	619463cd-6861-4f31-aa6e-eca589183af5	95557a6b-92c4-4913-a9c0-8e3c35c67aad	9c87404a-5033-4843-9a0f-94c0b3f8a67f
a8617afe-b89f-465b-8790-62f06f0db45e	t	2026-01-27 01:22:18.132774+02	20	90	Auto-assigned during system initialization	طب القلب	2026-01-27 01:22:18.132774+02	619463cd-6861-4f31-aa6e-eca589183af5	95557a6b-92c4-4913-a9c0-8e3c35c67aad	a953b13a-5982-4074-b4b3-1bb85357e4d9
a25fb3e7-3d51-4ac1-b1a7-3907ccf94a93	t	2026-01-27 01:22:18.132774+02	20	90	Auto-assigned during system initialization	طب القلب	2026-01-27 01:22:18.132774+02	619463cd-6861-4f31-aa6e-eca589183af5	95557a6b-92c4-4913-a9c0-8e3c35c67aad	8d752e35-af7c-40ee-aaea-a92b07a0801a
c4434a61-f692-4587-9ea1-e1ccb6098f34	t	2026-01-27 01:22:18.132774+02	20	90	Auto-assigned during system initialization	طب القلب	2026-01-27 01:22:18.132774+02	619463cd-6861-4f31-aa6e-eca589183af5	95557a6b-92c4-4913-a9c0-8e3c35c67aad	aea0c0ec-e4e0-4272-93de-e7081b05bad8
8e985bf1-6377-461c-8efc-ec663bcd8a06	t	2026-01-27 01:22:18.132774+02	20	90	Auto-assigned during system initialization	طب القلب	2026-01-27 01:22:18.132774+02	619463cd-6861-4f31-aa6e-eca589183af5	95557a6b-92c4-4913-a9c0-8e3c35c67aad	0a132861-308c-4a1d-a96f-2a7224cdb44d
6d67d7a9-e656-4266-973b-ea9d166cd9fe	t	2026-01-27 01:22:18.132774+02	5	30	Auto-assigned during system initialization	طب القلب	2026-01-27 01:22:18.132774+02	619463cd-6861-4f31-aa6e-eca589183af5	95557a6b-92c4-4913-a9c0-8e3c35c67aad	ceea5fa9-6de0-46af-ba87-787d80b187c4
470d8c69-139f-4594-8d32-da95d177f17a	t	2026-01-27 01:22:18.132774+02	5	30	Auto-assigned during system initialization	طب القلب	2026-01-27 01:22:18.132774+02	619463cd-6861-4f31-aa6e-eca589183af5	95557a6b-92c4-4913-a9c0-8e3c35c67aad	a0588a3a-9083-4005-a64a-d3c28e2ddc10
902b764b-697c-406b-9c50-06b95314095b	t	2026-01-27 01:22:18.132774+02	5	30	Auto-assigned during system initialization	طب القلب	2026-01-27 01:22:18.132774+02	619463cd-6861-4f31-aa6e-eca589183af5	95557a6b-92c4-4913-a9c0-8e3c35c67aad	67a0d29f-1680-468b-a2f0-98edc946c2a8
0ebffe77-a65b-43c3-b24a-755de3395844	t	2026-01-27 01:22:18.132774+02	20	90	Auto-assigned during system initialization	طب الأطفال	2026-01-27 01:22:18.132774+02	619463cd-6861-4f31-aa6e-eca589183af5	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	c1ea6c9f-5b3b-4e33-a849-5a131ba686a7
34adc721-0262-4e63-907a-88ed426f47f2	t	2026-01-27 01:22:18.132774+02	20	90	Auto-assigned during system initialization	طب الأطفال	2026-01-27 01:22:18.132774+02	619463cd-6861-4f31-aa6e-eca589183af5	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	f8fe3448-4f39-42b8-95b5-fb0fb1a2e2cd
07848def-d035-4073-863d-6d2f6804dee2	t	2026-01-27 01:22:18.132774+02	20	90	Auto-assigned during system initialization	طب الأطفال	2026-01-27 01:22:18.132774+02	619463cd-6861-4f31-aa6e-eca589183af5	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	ec1725b9-24b5-4c2d-acb0-8b994f68187b
bb4a0679-7268-4d25-986b-2809e92be331	t	2026-01-27 01:22:18.132774+02	20	90	Auto-assigned during system initialization	طب الأطفال	2026-01-27 01:22:18.132774+02	619463cd-6861-4f31-aa6e-eca589183af5	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	6ad30801-7df2-4d58-b5f9-45c59cf229fc
86d8c83e-ead0-4f02-b07c-1e2bf3d63553	t	2026-01-27 01:22:18.132774+02	20	90	Auto-assigned during system initialization	طب الأطفال	2026-01-27 01:22:18.132774+02	619463cd-6861-4f31-aa6e-eca589183af5	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	a953b13a-5982-4074-b4b3-1bb85357e4d9
80974600-088d-481e-8a90-f1a2c9d05cad	t	2026-01-27 01:22:18.133771+02	20	90	Auto-assigned during system initialization	طب الأطفال	2026-01-27 01:22:18.133771+02	619463cd-6861-4f31-aa6e-eca589183af5	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	8d752e35-af7c-40ee-aaea-a92b07a0801a
3e5b9beb-080f-417e-8e89-f4a7df68c3c0	t	2026-01-27 01:22:18.133771+02	20	90	Auto-assigned during system initialization	طب الأطفال	2026-01-27 01:22:18.133771+02	619463cd-6861-4f31-aa6e-eca589183af5	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	aea0c0ec-e4e0-4272-93de-e7081b05bad8
a69e9c24-b3ab-405c-bd16-3498e025eec1	t	2026-01-27 01:22:18.133771+02	20	90	Auto-assigned during system initialization	طب الباطنة	2026-01-27 01:22:18.133771+02	619463cd-6861-4f31-aa6e-eca589183af5	65ae7b21-d2bd-4685-a10c-76d406884d62	c1ea6c9f-5b3b-4e33-a849-5a131ba686a7
cf4e20ed-9c14-486b-bdf1-490938f4b7a3	t	2026-01-27 01:22:18.133771+02	20	90	Auto-assigned during system initialization	طب الباطنة	2026-01-27 01:22:18.133771+02	619463cd-6861-4f31-aa6e-eca589183af5	65ae7b21-d2bd-4685-a10c-76d406884d62	f8fe3448-4f39-42b8-95b5-fb0fb1a2e2cd
5beddc19-68c6-43dc-9434-95b3f0d4ea7f	t	2026-01-27 01:22:18.133771+02	20	90	Auto-assigned during system initialization	طب الباطنة	2026-01-27 01:22:18.133771+02	619463cd-6861-4f31-aa6e-eca589183af5	65ae7b21-d2bd-4685-a10c-76d406884d62	ec1725b9-24b5-4c2d-acb0-8b994f68187b
44588b5d-6f24-45d2-ba59-b02298ab679e	t	2026-01-27 01:22:18.133771+02	20	90	Auto-assigned during system initialization	طب الباطنة	2026-01-27 01:22:18.133771+02	619463cd-6861-4f31-aa6e-eca589183af5	65ae7b21-d2bd-4685-a10c-76d406884d62	6ad30801-7df2-4d58-b5f9-45c59cf229fc
bb3d2458-1d40-4b4a-9cea-1d404ef5587e	t	2026-01-27 01:22:18.133771+02	20	90	Auto-assigned during system initialization	طب الباطنة	2026-01-27 01:22:18.133771+02	619463cd-6861-4f31-aa6e-eca589183af5	65ae7b21-d2bd-4685-a10c-76d406884d62	c02002e4-6e56-4ab3-819d-9db6bf47f606
4a594186-74cb-48d3-9e55-fbf9cd995106	t	2026-01-27 01:22:18.133771+02	20	90	Auto-assigned during system initialization	طب الباطنة	2026-01-27 01:22:18.133771+02	619463cd-6861-4f31-aa6e-eca589183af5	65ae7b21-d2bd-4685-a10c-76d406884d62	9c87404a-5033-4843-9a0f-94c0b3f8a67f
6fd5bd20-43cf-4cbe-90e5-3e048274bdeb	t	2026-01-27 01:22:18.133771+02	20	90	Auto-assigned during system initialization	طب الباطنة	2026-01-27 01:22:18.133771+02	619463cd-6861-4f31-aa6e-eca589183af5	65ae7b21-d2bd-4685-a10c-76d406884d62	a953b13a-5982-4074-b4b3-1bb85357e4d9
e819ac88-5e61-4054-a0b6-aaf2f96f23f8	t	2026-01-27 01:22:18.133771+02	20	90	Auto-assigned during system initialization	طب الباطنة	2026-01-27 01:22:18.133771+02	619463cd-6861-4f31-aa6e-eca589183af5	65ae7b21-d2bd-4685-a10c-76d406884d62	8d752e35-af7c-40ee-aaea-a92b07a0801a
ff4cf9bc-1375-4613-8dd0-71125c576a80	t	2026-01-27 01:22:18.133771+02	20	90	Auto-assigned during system initialization	طب الباطنة	2026-01-27 01:22:18.133771+02	619463cd-6861-4f31-aa6e-eca589183af5	65ae7b21-d2bd-4685-a10c-76d406884d62	aea0c0ec-e4e0-4272-93de-e7081b05bad8
85eac9df-f3bd-4361-89d4-5c6398d8bbf0	t	2026-01-27 01:22:18.133771+02	20	90	Auto-assigned during system initialization	طب الباطنة	2026-01-27 01:22:18.133771+02	619463cd-6861-4f31-aa6e-eca589183af5	65ae7b21-d2bd-4685-a10c-76d406884d62	0a132861-308c-4a1d-a96f-2a7224cdb44d
03f052f9-7d96-4320-b06d-86e41161f9cd	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب العيون	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	434a147e-eaed-46fd-931b-d0ebaf4bcaea	c1ea6c9f-5b3b-4e33-a849-5a131ba686a7
ed6b672d-3722-4f3e-8bce-5ededf6d0f3e	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب العيون	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	434a147e-eaed-46fd-931b-d0ebaf4bcaea	f8fe3448-4f39-42b8-95b5-fb0fb1a2e2cd
d5d515d4-ecfd-480b-b5ef-719ef253b0c4	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب العيون	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	434a147e-eaed-46fd-931b-d0ebaf4bcaea	ec1725b9-24b5-4c2d-acb0-8b994f68187b
bb5e0e6d-d192-4216-8bb0-731c30140cc5	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب العيون	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	434a147e-eaed-46fd-931b-d0ebaf4bcaea	6ad30801-7df2-4d58-b5f9-45c59cf229fc
b5939a55-7abe-471b-86ae-da50fd6390c4	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب العيون	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	434a147e-eaed-46fd-931b-d0ebaf4bcaea	a953b13a-5982-4074-b4b3-1bb85357e4d9
1f52a2cc-5aa0-4222-8908-28a8b4c40d9a	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب العيون	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	434a147e-eaed-46fd-931b-d0ebaf4bcaea	8d752e35-af7c-40ee-aaea-a92b07a0801a
c490b919-b2ec-4fc7-9c42-e878db3b307b	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب العيون	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	434a147e-eaed-46fd-931b-d0ebaf4bcaea	aea0c0ec-e4e0-4272-93de-e7081b05bad8
107fb8f4-7b94-438b-be9e-5f79796bdd42	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب الأسنان	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	8c9de48c-10af-4670-aadb-9f9ebb9e75a1	c1ea6c9f-5b3b-4e33-a849-5a131ba686a7
33893d65-e32a-42e1-a76e-2ab6ba2ef687	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب الأسنان	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	8c9de48c-10af-4670-aadb-9f9ebb9e75a1	f8fe3448-4f39-42b8-95b5-fb0fb1a2e2cd
cf3fafcf-fd67-4cf3-be24-ba8e810f48ad	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب الأسنان	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	8c9de48c-10af-4670-aadb-9f9ebb9e75a1	ec1725b9-24b5-4c2d-acb0-8b994f68187b
1b864894-e33b-4972-89bf-4c409d2c13ff	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب الأسنان	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	8c9de48c-10af-4670-aadb-9f9ebb9e75a1	6ad30801-7df2-4d58-b5f9-45c59cf229fc
ba74fd55-262d-46f7-8596-a512d65aae9e	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب الأسنان	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	8c9de48c-10af-4670-aadb-9f9ebb9e75a1	a953b13a-5982-4074-b4b3-1bb85357e4d9
69791a1d-f5b8-4dbc-a971-8077bcf9db19	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب الأسنان	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	8c9de48c-10af-4670-aadb-9f9ebb9e75a1	8d752e35-af7c-40ee-aaea-a92b07a0801a
4139abb1-a39e-46e7-b0c6-cdffc0eaa412	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب الأسنان	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	8c9de48c-10af-4670-aadb-9f9ebb9e75a1	aea0c0ec-e4e0-4272-93de-e7081b05bad8
c27f14b9-aebb-4b24-a6b7-312edb91456f	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب النساء والتوليد	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	1c49d82a-0f7d-4b53-801e-74761df1008f	c1ea6c9f-5b3b-4e33-a849-5a131ba686a7
4472e4bf-cca7-4cd2-a3a6-1a64a873d350	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب النساء والتوليد	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	1c49d82a-0f7d-4b53-801e-74761df1008f	f8fe3448-4f39-42b8-95b5-fb0fb1a2e2cd
468f4968-dcd9-4ead-9b28-6d9a63e7c478	t	2026-01-27 01:22:18.134767+02	20	90	Auto-assigned during system initialization	طب النساء والتوليد	2026-01-27 01:22:18.134767+02	619463cd-6861-4f31-aa6e-eca589183af5	1c49d82a-0f7d-4b53-801e-74761df1008f	ec1725b9-24b5-4c2d-acb0-8b994f68187b
679e139a-93f2-4231-b419-3db6b4c6790a	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب النساء والتوليد	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	1c49d82a-0f7d-4b53-801e-74761df1008f	6ad30801-7df2-4d58-b5f9-45c59cf229fc
584496ce-d4a7-49e8-a7a0-93ff2816a1c4	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب النساء والتوليد	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	1c49d82a-0f7d-4b53-801e-74761df1008f	a953b13a-5982-4074-b4b3-1bb85357e4d9
9224c32b-00ce-4269-b28d-208841a0717a	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب النساء والتوليد	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	1c49d82a-0f7d-4b53-801e-74761df1008f	8d752e35-af7c-40ee-aaea-a92b07a0801a
e639e19a-6f65-40a3-81ad-50e3d025d26c	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب النساء والتوليد	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	1c49d82a-0f7d-4b53-801e-74761df1008f	aea0c0ec-e4e0-4272-93de-e7081b05bad8
1cc3bc01-fee1-4e97-be44-fb382453414d	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب العظام	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	c3ae6b3b-c835-448c-b0ad-298bec2c5cb3	c1ea6c9f-5b3b-4e33-a849-5a131ba686a7
815a37e2-61c0-4668-a9e5-ebd61d03cd0a	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب العظام	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	c3ae6b3b-c835-448c-b0ad-298bec2c5cb3	f8fe3448-4f39-42b8-95b5-fb0fb1a2e2cd
676b091f-f983-4416-bcf5-a7870d649c67	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب العظام	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	c3ae6b3b-c835-448c-b0ad-298bec2c5cb3	ec1725b9-24b5-4c2d-acb0-8b994f68187b
b6b51be6-a9b7-4751-a2c3-2e80242ed7d7	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب العظام	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	c3ae6b3b-c835-448c-b0ad-298bec2c5cb3	6ad30801-7df2-4d58-b5f9-45c59cf229fc
ebf535d9-afdc-4bb7-b543-676d114e4dcb	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب العظام	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	c3ae6b3b-c835-448c-b0ad-298bec2c5cb3	a953b13a-5982-4074-b4b3-1bb85357e4d9
248f1b16-eddf-4e63-91e1-bcc37ef466fd	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب العظام	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	c3ae6b3b-c835-448c-b0ad-298bec2c5cb3	8d752e35-af7c-40ee-aaea-a92b07a0801a
42f62208-4488-4325-a1f7-851b40843e1d	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب العظام	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	c3ae6b3b-c835-448c-b0ad-298bec2c5cb3	aea0c0ec-e4e0-4272-93de-e7081b05bad8
62bf803d-fcaf-4ae1-bbaf-4e927dbf7089	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب الجلدية	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	ae9f721f-4e97-4312-a680-daf79eaeba1f	c1ea6c9f-5b3b-4e33-a849-5a131ba686a7
8b0a197e-0a76-4c45-a8cc-6b155534b292	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب الجلدية	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	ae9f721f-4e97-4312-a680-daf79eaeba1f	f8fe3448-4f39-42b8-95b5-fb0fb1a2e2cd
d893a44d-b5c4-45a3-9d5e-876213afb8b8	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب الجلدية	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	ae9f721f-4e97-4312-a680-daf79eaeba1f	ec1725b9-24b5-4c2d-acb0-8b994f68187b
3ab1c341-b731-4a4b-b499-cc99e7ea7ee7	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب الجلدية	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	ae9f721f-4e97-4312-a680-daf79eaeba1f	6ad30801-7df2-4d58-b5f9-45c59cf229fc
12d6871a-7651-439c-ac7a-8eae18855bc7	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب الجلدية	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	ae9f721f-4e97-4312-a680-daf79eaeba1f	a953b13a-5982-4074-b4b3-1bb85357e4d9
b269c5d9-ab49-4cdb-95f0-11b5d3a9cad0	t	2026-01-27 01:22:18.135765+02	20	90	Auto-assigned during system initialization	طب الجلدية	2026-01-27 01:22:18.135765+02	619463cd-6861-4f31-aa6e-eca589183af5	ae9f721f-4e97-4312-a680-daf79eaeba1f	8d752e35-af7c-40ee-aaea-a92b07a0801a
9fd25199-e1ea-4ea2-a380-45d447e35cae	t	2026-01-27 01:22:18.136761+02	20	90	Auto-assigned during system initialization	طب الجلدية	2026-01-27 01:22:18.136761+02	619463cd-6861-4f31-aa6e-eca589183af5	ae9f721f-4e97-4312-a680-daf79eaeba1f	aea0c0ec-e4e0-4272-93de-e7081b05bad8
\.


--
-- Data for Name: doctor_procedures; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.doctor_procedures (id, active, category, coverage_percentage, coverage_status, created_at, max_price, price, procedure_name, updated_at) FROM stdin;
b029e60b-1eae-426d-8b9e-f239c04c00c7	t	GENERAL	100	COVERED	2026-01-27 01:22:18.064241+02	\N	30.00	ECG Normal	2026-01-27 01:22:18.064241+02
a459b3c4-6eef-4d0d-812b-51bacd88eb03	t	GENERAL	100	COVERED	2026-01-27 01:22:18.064241+02	\N	70.00	ECG with Report	2026-01-27 01:22:18.064241+02
61aaf2ac-14a0-4334-a691-27ddfa2984d8	t	GENERAL	100	COVERED	2026-01-27 01:22:18.064241+02	\N	10.00	Injection (I.M)	2026-01-27 01:22:18.064241+02
8902cfb1-d06f-4bb2-ab2c-04d1020ae1d4	t	GENERAL	100	COVERED	2026-01-27 01:22:18.064241+02	\N	20.00	I.V Line Set + Canula	2026-01-27 01:22:18.064241+02
4d8cbc81-f110-44bc-90b3-b06076dcb521	t	GENERAL	100	COVERED	2026-01-27 01:22:18.064241+02	45.00	30.00	Dressing (Small)	2026-01-27 01:22:18.064241+02
0fe7bd09-bcb8-4521-8c76-b042fcd36e02	t	GENERAL	100	COVERED	2026-01-27 01:22:18.064241+02	70.00	50.00	Dressing (Large)	2026-01-27 01:22:18.064241+02
afa48982-8a70-4902-96c8-9cd03a776c8a	t	CARDIOLOGY	100	COVERED	2026-01-27 01:22:18.064241+02	\N	200.00	Echo Cardiogram	2026-01-27 01:22:18.064241+02
6daf713d-db1b-4ed7-8008-5b1939e5da6c	t	CARDIOLOGY	100	COVERED	2026-01-27 01:22:18.064241+02	\N	175.00	Stress Test	2026-01-27 01:22:18.064241+02
230d63b6-9e91-406c-be34-7bade9a7fbf0	t	CARDIOLOGY	100	COVERED	2026-01-27 01:22:18.064241+02	\N	175.00	Holter Monitor 24hr	2026-01-27 01:22:18.064241+02
e205778c-04e6-41f8-9653-697f3a4cb4fc	t	CARDIOLOGY	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.064241+02	\N	300.00	ABPM	2026-01-27 01:22:18.064241+02
f63bd786-bdfb-4b93-83bd-e05fd20d8577	t	SURGERY	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.064241+02	500.00	150.00	Minor Surgery	2026-01-27 01:22:18.064241+02
0864f908-0be0-4b90-a206-3d199ce425b8	t	SURGERY	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.064241+02	\N	500.00	Skin Biopsy	2026-01-27 01:22:18.064241+02
3b665572-561f-4955-934a-c1b525c6ad18	t	SURGERY	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.064241+02	\N	575.00	Lipoma Excision	2026-01-27 01:22:18.064241+02
709d8d64-d1a1-41fd-b555-70e0409d3506	t	SURGERY	100	COVERED	2026-01-27 01:22:18.064241+02	\N	600.00	Ingrown Toenail Removal	2026-01-27 01:22:18.064241+02
e2519941-e70c-42a0-a974-0075b472c060	t	SURGERY	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.064241+02	\N	575.00	Sebaceous Cyst Excision	2026-01-27 01:22:18.064241+02
73daa9db-a924-4a83-b213-50d6d18d4fc7	t	SURGERY	100	COVERED	2026-01-27 01:22:18.064241+02	\N	300.00	Circumcision	2026-01-27 01:22:18.064241+02
8cce9168-ada3-416f-a4a0-0933d1bae472	t	ENT	100	COVERED	2026-01-27 01:22:18.065222+02	\N	150.00	Nasal Cautery	2026-01-27 01:22:18.065222+02
12fbea40-fa8f-48e7-960f-585102eec1ed	t	ENT	100	COVERED	2026-01-27 01:22:18.065222+02	\N	80.00	Ear Irrigation	2026-01-27 01:22:18.065222+02
eada076a-dafd-4eba-83f6-ddafcd94fe23	t	ENT	100	COVERED	2026-01-27 01:22:18.065222+02	\N	150.00	Audiogram	2026-01-27 01:22:18.065222+02
d9fb48d1-6251-4d40-85ea-1aa4a22febc7	t	ENT	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.065222+02	\N	200.00	Laryngoscopy	2026-01-27 01:22:18.065222+02
ae51aa3e-7710-49d7-8021-dfc51e449e52	t	ENT	100	COVERED	2026-01-27 01:22:18.065222+02	\N	250.00	Foreign Body Removal	2026-01-27 01:22:18.065222+02
6574a51e-89c5-4a02-99b5-405cfe3df501	t	ORTHOPEDIC	100	COVERED	2026-01-27 01:22:18.065222+02	\N	235.00	Cast (Below Elbow)	2026-01-27 01:22:18.065222+02
003f4b7e-6731-42d8-9f8c-90d6adcd9bfa	t	ORTHOPEDIC	100	COVERED	2026-01-27 01:22:18.065222+02	\N	250.00	Cast (Above Elbow)	2026-01-27 01:22:18.065222+02
c69f00e8-327a-404a-8d41-7558153596a9	t	ORTHOPEDIC	100	COVERED	2026-01-27 01:22:18.065222+02	\N	235.00	Cast (Below Knee)	2026-01-27 01:22:18.065222+02
db22ab84-45a6-469b-a68c-de6bec10663e	t	ORTHOPEDIC	100	COVERED	2026-01-27 01:22:18.065222+02	\N	300.00	Cast (Above Knee)	2026-01-27 01:22:18.065222+02
743f1379-9b84-4edf-8269-5db6df1901ff	t	ORTHOPEDIC	100	COVERED	2026-01-27 01:22:18.065222+02	\N	100.00	Joint Injection	2026-01-27 01:22:18.065222+02
c614ffe3-34a2-4633-a5c6-c6cb31df3fa9	t	ORTHOPEDIC	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.065222+02	\N	130.00	Arthrocentesis	2026-01-27 01:22:18.065222+02
db871add-249b-4c34-a9ce-e61229659d63	t	OBGYN	100	COVERED	2026-01-27 01:22:18.065222+02	\N	100.00	Pap Smear	2026-01-27 01:22:18.065222+02
ceaba56c-f4d8-47fc-b244-43ac6e097d8f	t	OBGYN	100	COVERED	2026-01-27 01:22:18.065222+02	\N	300.00	IUD Insertion	2026-01-27 01:22:18.065222+02
e02a42bb-7724-422c-961a-1208a4d3caea	t	OBGYN	100	COVERED	2026-01-27 01:22:18.065222+02	\N	100.00	IUD Removal	2026-01-27 01:22:18.065222+02
3f905cdc-a673-4f8d-ba52-f735d7581bda	t	OBGYN	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.065222+02	\N	250.00	Cervical Cautery	2026-01-27 01:22:18.065222+02
03254ebc-b922-4db7-98bf-4b8af3ac490b	t	OBGYN	100	COVERED	2026-01-27 01:22:18.065222+02	\N	50.00	NST (Non-Stress Test)	2026-01-27 01:22:18.065222+02
\.


--
-- Data for Name: doctor_specialization; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.doctor_specialization (id, consultation_price, diagnoses, diagnosis_treatment_mappings, display_name, gender_restriction, max_age, min_age, treatment_plans) FROM stdin;
1	150	{"ارتفاع ضغط الدم","قصور القلب","اضطراب نبضات القلب","الذبحة الصدرية"}	\N	طب القلب	\N	\N	\N	{"أدوية الضغط","منظم ضربات القلب","قسطرة قلبية"}
3	100	{السكري,"ارتفاع الكوليسترول","التهاب المعدة","فقر الدم"}	\N	طب الباطنة	\N	\N	\N	{"أدوية السكري","أدوية الكوليسترول","مكملات الحديد"}
4	120	{"قصر النظر","المياه البيضاء","التهاب الملتحمة","جفاف العين"}	\N	طب العيون	\N	\N	\N	{"نظارات طبية","عملية الليزك","قطرات العين"}
5	90	{"تسوس الأسنان","التهاب اللثة","خلع ضرس العقل"}	\N	طب الأسنان	\N	\N	\N	{"حشو الأسنان","تنظيف اللثة","خلع الأسنان"}
6	130	{"متابعة الحمل","تكيس المبايض","اضطرابات الدورة"}	\N	طب النساء والتوليد	FEMALE	\N	\N	{"فحص الحمل",سونار,"أدوية هرمونية"}
7	110	{"كسور العظام","التهاب المفاصل","آلام الظهر","الانزلاق الغضروفي"}	\N	طب العظام	\N	\N	\N	{جبيرة,"علاج طبيعي",مسكنات}
8	100	{"حب الشباب",الأكزيما,الصدفية,الثعلبة}	\N	طب الجلدية	\N	\N	\N	{"كريمات موضعية","مضادات الهيستامين","علاج ضوئي"}
9	140	{"الصداع النصفي",الصرع,"التصلب اللويحي","الشلل الرعاش"}	\N	طب الأعصاب	\N	\N	\N	{"مضادات الصرع","مسكنات الأعصاب","علاج طبيعي"}
10	160	{الاكتئاب,القلق,الأرق,"الوسواس القهري"}	\N	الطب النفسي	\N	\N	\N	{"مضادات الاكتئاب",مهدئات,"جلسات علاج نفسي"}
2	80	{"التهاب الحلق","التهاب الأذن",الحمى,الإسهال}	\N	طب الأطفال	\N	18	0	{"مضادات حيوية","خافض حرارة",محاليل}
\.


--
-- Data for Name: doctor_specialization_allowed_genders; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.doctor_specialization_allowed_genders (specialization_id, gender) FROM stdin;
6	FEMALE
\.


--
-- Data for Name: doctor_test_assignments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.doctor_test_assignments (id, active, assigned_at, max_daily_requests, notes, specialization, test_type, updated_at, assigned_by, doctor_id, test_id) FROM stdin;
\.


--
-- Data for Name: emergency_requests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.emergency_requests (id, approved_at, contact_phone, created_at, description, doctor_id, family_member_id, incident_date, location, notes, rejected_at, rejection_reason, status, submitted_at, updated_at, member_id) FROM stdin;
\.


--
-- Data for Name: family_member_documents; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.family_member_documents (family_member_id, document_path) FROM stdin;
\.


--
-- Data for Name: family_members; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.family_members (id, created_at, date_of_birth, full_name, gender, insurance_number, national_id, relation, status, client_id) FROM stdin;
82a3cf72-118b-494b-9207-b73e39d088fd	2026-01-27 01:22:16.008658+02	1988-05-10	سمية أحمد محمد	FEMALE	EMP6000000001.01	FM600000001	WIFE	APPROVED	20f27a3d-4257-438d-a327-ae327c0b29ce
5440c477-7bc1-48a3-aed4-0326a70ad712	2026-01-27 01:22:16.013633+02	2015-08-20	يوسف محمد أحمد	MALE	EMP6000000001.02	FM600000002	SON	APPROVED	20f27a3d-4257-438d-a327-ae327c0b29ce
3a2ebe1a-f7f4-4755-a2c1-7dbe438178cf	2026-01-27 01:22:16.015628+02	2018-03-15	مريم محمد أحمد	FEMALE	EMP6000000001.03	FM600000003	DAUGHTER	APPROVED	20f27a3d-4257-438d-a327-ae327c0b29ce
d8dfec0e-ef13-495c-a7ee-3c0a2fd79fbf	2026-01-27 01:22:16.01862+02	1955-11-25	أحمد عبدالله محمد	MALE	EMP6000000001.04	FM600000004	FATHER	APPROVED	20f27a3d-4257-438d-a327-ae327c0b29ce
cd8c90ee-5e2b-4a62-99d3-e1a0cfc833b6	2026-01-27 01:22:16.021613+02	1960-06-08	خديجة سعيد علي	FEMALE	EMP6000000001.05	FM600000005	MOTHER	APPROVED	20f27a3d-4257-438d-a327-ae327c0b29ce
8a6d0438-5e64-44d8-9cea-fcece31e38a3	2026-01-27 01:55:51.39525+02	1987-04-15	أحمد محمد سعيد	MALE	EMP6000000002.01	PFM200001	HUSBAND	PENDING	10da5d69-b91c-454b-b084-1c772dac7fa8
b84a2e80-d111-4638-91e5-2362bde20741	2026-01-27 01:55:51.399241+02	2015-09-22	ليلى محمد سعيد	FEMALE	EMP6000000002.02	PFM200002	DAUGHTER	PENDING	10da5d69-b91c-454b-b084-1c772dac7fa8
c74ad479-13ef-4a00-ba44-5f4fb0c783e7	2026-01-27 01:55:51.401235+02	2018-01-10	يوسف محمد سعيد	MALE	EMP6000000002.03	PFM200003	SON	PENDING	10da5d69-b91c-454b-b084-1c772dac7fa8
16d60f17-b16e-4161-8649-d74cb124fe33	2026-01-27 01:55:51.403845+02	1985-11-05	سامي حسن علي	MALE	EMP6000000004.01	PFM400001	HUSBAND	PENDING	48993e40-0bf9-4412-9b0b-9817cf6b920f
05cc42bf-eed1-4f64-934a-a772d8495984	2026-01-27 01:55:51.405843+02	1958-03-20	جمال حسن علي	MALE	EMP6000000004.02	PFM400002	FATHER	PENDING	48993e40-0bf9-4412-9b0b-9817cf6b920f
8dd1db0d-7d90-483d-895a-9a5e4ba77f46	2026-01-27 01:55:51.407837+02	1962-07-08	سعاد أحمد محمود	FEMALE	EMP6000000004.03	PFM400003	MOTHER	PENDING	48993e40-0bf9-4412-9b0b-9817cf6b920f
64bb63c4-864f-4e3d-b897-d95e7902e478	2026-01-27 01:55:51.408834+02	1997-06-18	سارة أحمد عمر	FEMALE	EMP6000000005.01	PFM500001	WIFE	PENDING	6a2df59a-141b-4fc7-85f4-b71195df9fb8
a3b8257a-e6d2-4f3e-b0a7-2ef38ccbbbc8	2026-01-27 01:55:51.410829+02	2021-12-03	آدم عمر سعيد	MALE	EMP6000000005.02	PFM500002	SON	PENDING	6a2df59a-141b-4fc7-85f4-b71195df9fb8
eb4648e9-b554-4b78-be76-b52683c17d1d	2026-01-27 01:55:51.412823+02	1990-02-25	خالد محمد أحمد	MALE	EMP6000000006.01	PFM600001	HUSBAND	PENDING	8508189c-6f4f-482a-9d99-433ba0270084
bfaf1957-42b4-459e-a61c-8a4cf371ac36	2026-01-27 01:55:51.414362+02	2019-08-14	نور سارة خالد	FEMALE	EMP6000000006.02	PFM600002	DAUGHTER	PENDING	8508189c-6f4f-482a-9d99-433ba0270084
434beff7-e33f-49b0-8814-0cb74d726b4b	2026-01-27 01:55:51.416359+02	1960-05-30	محمد أحمد خالد	MALE	EMP6000000006.03	PFM600003	FATHER	PENDING	8508189c-6f4f-482a-9d99-433ba0270084
\.


--
-- Data for Name: healthcare_provider_claims; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.healthcare_provider_claims (id, amount, approved_at, client_id, client_name, client_pay_amount, coverage_message, coverage_percent_used, description, diagnosis, doctor_name, emergency, insurance_covered_amount, invoice_image_path, is_chronic, is_covered, is_follow_up, max_coverage_used, medical_reviewed_at, medical_reviewer_id, medical_reviewer_name, original_consultation_fee, paid_at, paid_by, rejected_at, rejection_reason, role_specific_data, service_date, status, submitted_at, treatment_details, provider_id, policy_id) FROM stdin;
aba0bcb6-801b-47a5-8fdd-58deebd85809	120	2026-01-15 02:12:19.048752+02	10da5d69-b91c-454b-b084-1c772dac7fa8	فاطمة محمد سعيد	24.00	\N	80.00	فحص أطفال روتيني	فحص دوري - نمو طبيعي	\N	\N	96.00	\N	f	t	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-01-12	APPROVED_FINAL	2026-01-27 02:12:19.048752+02	متابعة التطعيمات	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	11aea7b1-5ee0-43a0-a746-52006c165769
cc18c649-0d9d-4085-9663-847ce1293d35	85	2026-01-20 02:12:19.052742+02	20f27a3d-4257-438d-a327-ae327c0b29ce	محمد أحمد عبدالله	17.00	\N	80.00	صرف أدوية - مسكنات	\N	\N	\N	68.00	\N	f	t	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	باراسيتامول 500mg x30\nإيبوبروفين 400mg x20	2026-01-18	APPROVED_FINAL	2026-01-27 02:12:19.052742+02	\N	584a36c8-794c-42c6-9c09-e768dc32ee30	11aea7b1-5ee0-43a0-a746-52006c165769
52ba29e0-06ed-4dc6-8578-a4e8ddb00905	180	2026-01-09 02:12:19.05374+02	f1d42ef1-3c2d-400f-b6d9-996309ec35d8	أحمد خالد عمر	36.00	\N	80.00	فحوصات مخبرية شاملة	\N	\N	\N	144.00	\N	f	t	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	فحص دم شامل CBC\nفحص السكر صائم\nفحص الدهون الشامل	2026-01-07	APPROVED_FINAL	2026-01-27 02:12:19.054737+02	\N	4f789b0d-9385-485e-b9c7-a586af5cabd1	9d7e59f0-6801-41a4-905a-23bf1ab663e1
caaab263-a678-4cb1-90ec-f23827b902ff	80	2026-01-05 02:12:19.055734+02	48993e40-0bf9-4412-9b0b-9817cf6b920f	نور حسن علي	16.00	\N	80.00	أشعة صدر	\N	\N	\N	64.00	\N	f	t	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	أشعة صدر X-Ray - النتيجة طبيعية	2026-01-02	APPROVED_FINAL	2026-01-27 02:12:19.055734+02	\N	48238880-3a07-4d63-8c1f-e7f5fc54da3b	9d7e59f0-6801-41a4-905a-23bf1ab663e1
d8e123de-4e14-4070-abd1-787356bf01bc	5000	2026-01-02 02:12:19.056732+02	6a2df59a-141b-4fc7-85f4-b71195df9fb8	عمر سعيد محمد	0.00	\N	0.00	عملية تجميلية	طلب عملية تجميل الأنف	\N	\N	0.00	\N	f	f	f	\N	\N	\N	\N	\N	\N	\N	2026-01-03 02:12:19.058727+02	العمليات التجميلية غير مغطاة بالتأمين الصحي	\N	2025-12-28	REJECTED_FINAL	2026-01-27 02:12:19.056732+02	عملية تجميلية غير طبية	65ae7b21-d2bd-4685-a10c-76d406884d62	6322e814-3596-41a9-9943-a31db4b2813e
ac7b770c-d577-4183-88f5-39ae4f3809a3	250	2026-01-12 02:12:19.068701+02	48993e40-0bf9-4412-9b0b-9817cf6b920f	نور حسن علي	0.00	\N	0.00	مكملات غذائية غير مغطاة	\N	\N	\N	0.00	\N	f	f	f	\N	\N	\N	\N	\N	\N	\N	2026-01-13 02:12:19.069699+02	المكملات الغذائية غير الطبية غير مشمولة بالتغطية	فيتامينات متعددة\nمكملات البروتين	2026-01-09	REJECTED_FINAL	2026-01-27 02:12:19.068701+02	\N	01f4d329-987d-4560-98bf-298a3ece3f27	9d7e59f0-6801-41a4-905a-23bf1ab663e1
a988bc8d-6e5a-463c-9cc7-01fb849fc918	130	\N	f1d42ef1-3c2d-400f-b6d9-996309ec35d8	أحمد خالد عمر	0.00	\N	0.00	فحص طفل - حرارة مرتفعة	التهاب حلق حاد	\N	\N	0.00	\N	f	f	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-01-26	PENDING_MEDICAL	2026-01-27 02:12:19.077677+02	مضاد حيوي وخافض حرارة	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	9d7e59f0-6801-41a4-905a-23bf1ab663e1
c52a0c94-ca8f-45d4-8866-5ce9180df5f3	145	\N	20f27a3d-4257-438d-a327-ae327c0b29ce	محمد أحمد عبدالله	0.00	\N	0.00	فحوصات الغدة الدرقية	\N	\N	\N	0.00	\N	f	f	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	فحص الغدة الدرقية TSH\nفحص T3 و T4	2026-01-27	PENDING_MEDICAL	2026-01-27 02:12:19.078674+02	\N	0760c6f6-4000-4aba-8357-86ef3e8f392a	11aea7b1-5ee0-43a0-a746-52006c165769
6cae0cd4-46e7-4528-a4b1-6b5130876bb3	95	\N	6a2df59a-141b-4fc7-85f4-b71195df9fb8	عمر سعيد محمد	0.00	\N	0.00	صرف أدوية السكري	\N	\N	\N	0.00	\N	f	f	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	ميتفورمين 850mg x60\nجهاز قياس السكر	2026-01-24	PENDING_MEDICAL	2026-01-27 02:12:19.079671+02	\N	584a36c8-794c-42c6-9c09-e768dc32ee30	6322e814-3596-41a9-9943-a31db4b2813e
497eb03d-3dab-49b1-992f-fbb85c83b9a8	180	\N	48993e40-0bf9-4412-9b0b-9817cf6b920f	نور حسن علي	0.00	\N	0.00	استشارة باطنية - آلام المعدة	التهاب المعدة المزمن	\N	\N	0.00	\N	f	f	f	\N	\N	\N	\N	\N	\N	\N	\N	يرجى إرفاق نتائج المنظار السابقة للمراجعة	\N	2026-01-22	RETURNED_FOR_REVIEW	2026-01-27 02:12:19.080676+02	أدوية وحمية غذائية	65ae7b21-d2bd-4685-a10c-76d406884d62	9d7e59f0-6801-41a4-905a-23bf1ab663e1
ff051945-cb85-473a-b435-26a3609255a9	80	\N	3a2ebe1a-f7f4-4755-a2c1-7dbe438178cf	مريم محمد أحمد	80.00	❌ Service not covered under this policy	\N	Medical consultation - طب الأطفال	التهاب الحلق	\N	\N	0.00	\N	\N	f	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-01-27	PENDING_MEDICAL	2026-01-27 06:07:45.129837+02	dsafas	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	\N
\.


--
-- Data for Name: lab_requests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.lab_requests (id, approved_price, created_at, diagnosis, entered_price, notes, result_url, status, test_name, treatment, updated_at, doctor_id, lab_tech_id, member_id, price_id) FROM stdin;
c8a44fab-5a75-46eb-b36d-117c52e25f26	\N	2026-01-27 05:39:17.893296+02	الحمى	\N	الحمى	\N	PENDING	فحص البول الكامل	خافض حرارة	2026-01-27 05:39:17.893296+02	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	\N	20f27a3d-4257-438d-a327-ae327c0b29ce	ad1b7629-eebe-4e9b-bd96-6fe0dcdc6962
ede3bbc9-57d9-4394-a34b-705d30daf318	\N	2026-01-27 05:39:29.751274+02	الحمى	\N	الحمى	\N	PENDING	فحص البول الكامل	خافض حرارة	2026-01-27 05:39:29.751274+02	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	\N	20f27a3d-4257-438d-a327-ae327c0b29ce	ad1b7629-eebe-4e9b-bd96-6fe0dcdc6962
\.


--
-- Data for Name: medical_diagnoses; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.medical_diagnoses (id, active, arabic_name, created_at, description, english_name, updated_at) FROM stdin;
9d7799d1-d3b9-41f1-a7f5-4d4cbe1b3d29	t	ارتفاع ضغط الدم	2026-01-27 01:22:18.069212+02	\N	Hypertension	2026-01-27 01:22:18.069212+02
3aef2dc9-90a7-4419-ab23-4f6f1aad7c34	t	السكري النوع الثاني	2026-01-27 01:22:18.069212+02	\N	Diabetes Mellitus Type 2	2026-01-27 01:22:18.069212+02
b087ccda-83ba-4590-b060-bd38aed4b2fa	t	السكري النوع الأول	2026-01-27 01:22:18.069212+02	\N	Diabetes Mellitus Type 1	2026-01-27 01:22:18.069212+02
c17caf35-39da-46ab-82d9-35ce0eeba759	t	الربو	2026-01-27 01:22:18.069212+02	\N	Asthma	2026-01-27 01:22:18.069212+02
8f1fd366-bd1a-48fd-9688-4f44803b532b	t	مرض الانسداد الرئوي المزمن	2026-01-27 01:22:18.069212+02	\N	Chronic Obstructive Pulmonary Disease	2026-01-27 01:22:18.069212+02
b77b436d-71ee-4b01-8f31-d3d1b8bad74d	t	مرض الشريان التاجي	2026-01-27 01:22:18.069212+02	\N	Coronary Artery Disease	2026-01-27 01:22:18.069212+02
f2d82f17-b9f7-430e-9709-9ff27347de39	t	فشل القلب	2026-01-27 01:22:18.069212+02	\N	Heart Failure	2026-01-27 01:22:18.069212+02
55549dbe-67c3-466d-bd08-9031b3f2a0f3	t	الرجفان الأذيني	2026-01-27 01:22:18.069212+02	\N	Atrial Fibrillation	2026-01-27 01:22:18.069212+02
30ec3acb-e969-4e74-9326-262900cff0f6	t	مرض الكلى المزمن	2026-01-27 01:22:18.069212+02	\N	Chronic Kidney Disease	2026-01-27 01:22:18.069212+02
0126eca4-c6c5-4f9d-ae95-1614df9306f9	t	التهاب المعدة	2026-01-27 01:22:18.069212+02	\N	Gastritis	2026-01-27 01:22:18.069212+02
e1a36c09-b041-4f63-9023-c970152a3f2d	t	مرض الارتجاع المعدي المريئي	2026-01-27 01:22:18.069212+02	\N	Gastroesophageal Reflux Disease	2026-01-27 01:22:18.069212+02
bed404a2-81de-4a56-a729-5673652f8c0e	t	مرض القرحة الهضمية	2026-01-27 01:22:18.069212+02	\N	Peptic Ulcer Disease	2026-01-27 01:22:18.069212+02
6be75de0-4699-4f00-9b3d-7cd1efd8c542	t	الصداع النصفي	2026-01-27 01:22:18.069212+02	\N	Migraine	2026-01-27 01:22:18.069212+02
f733f44f-135d-4c96-bb69-a6fd7f969c34	t	صداع التوتر	2026-01-27 01:22:18.069212+02	\N	Tension Headache	2026-01-27 01:22:18.069212+02
19a0995c-3ac2-46e5-9116-3e50ee8db1dd	t	الاكتئاب	2026-01-27 01:22:18.069212+02	\N	Depression	2026-01-27 01:22:18.069212+02
bec53fbf-b99d-423b-ae2e-95e1aa184681	t	اضطراب القلق	2026-01-27 01:22:18.07021+02	\N	Anxiety Disorder	2026-01-27 01:22:18.07021+02
21775cd6-c416-4e43-8135-46e85416d9f4	t	قصور الغدة الدرقية	2026-01-27 01:22:18.07021+02	\N	Hypothyroidism	2026-01-27 01:22:18.07021+02
3d3fdaef-ced5-4ab1-b1e4-1c33248bf6f8	t	فرط نشاط الغدة الدرقية	2026-01-27 01:22:18.07021+02	\N	Hyperthyroidism	2026-01-27 01:22:18.07021+02
8ea87047-a645-40c0-8f17-3269d4dd9a35	t	هشاشة العظام	2026-01-27 01:22:18.07021+02	\N	Osteoporosis	2026-01-27 01:22:18.07021+02
7ec8966f-2f70-4b55-a994-0a182242b069	t	التهاب المفاصل التنكسي	2026-01-27 01:22:18.07021+02	\N	Osteoarthritis	2026-01-27 01:22:18.07021+02
ff4c6083-7db1-44c2-9300-0e6305ffd787	t	التهاب المفاصل الروماتويدي	2026-01-27 01:22:18.07021+02	\N	Rheumatoid Arthritis	2026-01-27 01:22:18.07021+02
0b3bb961-ab16-49ee-99cf-66a196374b8f	t	فقر الدم	2026-01-27 01:22:18.07021+02	\N	Anemia	2026-01-27 01:22:18.07021+02
90389afb-7843-4885-8e8f-1f3190851680	t	فقر الدم بعوز الحديد	2026-01-27 01:22:18.07021+02	\N	Iron Deficiency Anemia	2026-01-27 01:22:18.07021+02
9b773101-24f7-4817-ab79-9b3ad7f44e92	t	نقص فيتامين د	2026-01-27 01:22:18.07021+02	\N	Vitamin D Deficiency	2026-01-27 01:22:18.07021+02
da5cf744-e444-4530-89fa-8f1d91e5456b	t	عدوى الجهاز التنفسي العلوي	2026-01-27 01:22:18.07021+02	\N	Upper Respiratory Tract Infection	2026-01-27 01:22:18.07021+02
880da43f-1193-409e-a8ef-1524de88f84e	t	عدوى المسالك البولية	2026-01-27 01:22:18.07021+02	\N	Urinary Tract Infection	2026-01-27 01:22:18.07021+02
6cf0a6c2-7665-463c-98a4-bf1988b4a937	t	الالتهاب الرئوي	2026-01-27 01:22:18.07021+02	\N	Pneumonia	2026-01-27 01:22:18.07021+02
79dfda95-5b2c-4425-b74d-06e717cff038	t	التهاب الشعب الهوائية	2026-01-27 01:22:18.07021+02	\N	Bronchitis	2026-01-27 01:22:18.07021+02
c5b94eb1-5428-47e3-9cc2-0e2268fd868b	t	التهاب الجيوب الأنفية	2026-01-27 01:22:18.07021+02	\N	Sinusitis	2026-01-27 01:22:18.07021+02
388dfc6d-f7d9-4ac7-bbf5-b669d8dfa94d	t	التهاب الأنف التحسسي	2026-01-27 01:22:18.07021+02	\N	Allergic Rhinitis	2026-01-27 01:22:18.07021+02
51cd9adf-6229-42ec-9d30-db22f31eab6a	t	الإكزيما	2026-01-27 01:22:18.07021+02	\N	Eczema	2026-01-27 01:22:18.07021+02
40338ba5-33a3-4966-98ad-2669f0b46879	t	الصدفية	2026-01-27 01:22:18.07021+02	\N	Psoriasis	2026-01-27 01:22:18.07021+02
04449722-9991-41a2-9448-e5e58198e6c4	t	حب الشباب	2026-01-27 01:22:18.07021+02	\N	Acne Vulgaris	2026-01-27 01:22:18.07021+02
6a94b88f-1f3d-4090-bc0e-9129737cf4e2	t	آلام أسفل الظهر	2026-01-27 01:22:18.07021+02	\N	Lower Back Pain	2026-01-27 01:22:18.07021+02
9d62d01c-8857-4054-874f-61fa3b712ecb	t	داء الفقار الرقبي	2026-01-27 01:22:18.07021+02	\N	Cervical Spondylosis	2026-01-27 01:22:18.07021+02
fd38e64b-63b1-436e-ac94-142edfaf1677	t	عرق النسا	2026-01-27 01:22:18.07021+02	\N	Sciatica	2026-01-27 01:22:18.07021+02
\.


--
-- Data for Name: medical_records; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.medical_records (id, created_at, diagnosis, notes, treatment, updated_at, doctor_id, member_id) FROM stdin;
\.


--
-- Data for Name: medical_tests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.medical_tests (id, active, category, coverage_percentage, coverage_status, created_at, price, test_name, updated_at) FROM stdin;
9e1b73d8-b940-40f1-8329-24773c4b9112	t	LAB	100	COVERED	2026-01-27 01:22:18.047258+02	50.00	CBC (Complete Blood Count)	2026-01-27 01:22:18.047258+02
23a0479a-6174-4b5d-b50d-92c203a69014	t	LAB	100	COVERED	2026-01-27 01:22:18.047258+02	30.00	Fasting Blood Sugar	2026-01-27 01:22:18.047258+02
631cb5c4-d32b-4a70-9068-0c4100e60cc6	t	LAB	100	COVERED	2026-01-27 01:22:18.047258+02	80.00	Lipid Profile	2026-01-27 01:22:18.047258+02
9436de7d-279d-46c2-ae13-c2be0f9e258c	t	LAB	100	COVERED	2026-01-27 01:22:18.047258+02	90.00	Liver Function Test (LFT)	2026-01-27 01:22:18.047258+02
35b84a81-3bac-4f7a-b7d5-c883a4636ee1	t	LAB	100	COVERED	2026-01-27 01:22:18.047258+02	85.00	Kidney Function Test (KFT)	2026-01-27 01:22:18.047258+02
3ad2c106-9bb9-42a1-9425-54aa28994d45	t	LAB	100	COVERED	2026-01-27 01:22:18.047258+02	120.00	Thyroid Panel (TSH/T3/T4)	2026-01-27 01:22:18.047258+02
898a6869-26eb-4403-a3f5-2907fd17daf3	t	LAB	100	COVERED	2026-01-27 01:22:18.047258+02	25.00	Urinalysis	2026-01-27 01:22:18.047258+02
19b7ec2a-1290-4db7-8d3d-e3ef70457823	t	LAB	100	COVERED	2026-01-27 01:22:18.047258+02	70.00	HbA1c (Glycated Hemoglobin)	2026-01-27 01:22:18.047258+02
d5821492-5c67-476e-bcf5-99252341d3ab	t	LAB	100	COVERED	2026-01-27 01:22:18.047258+02	60.00	Electrolytes Panel	2026-01-27 01:22:18.047258+02
15d0da72-ee77-4735-bc4e-8ed4659755fe	t	LAB	100	COVERED	2026-01-27 01:22:18.047258+02	20.00	ESR (Erythrocyte Sedimentation Rate)	2026-01-27 01:22:18.047258+02
f4623e33-5b68-4308-a9f2-4295fdb0130f	t	LAB	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.047258+02	120.00	Vitamin D	2026-01-27 01:22:18.047258+02
0a2dd9b5-6080-4cf9-b82c-af9f82512b32	t	LAB	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.047258+02	100.00	Vitamin B12	2026-01-27 01:22:18.047258+02
4ccf3d56-404c-4042-9176-0c7f69265011	t	LAB	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.047258+02	110.00	Iron Studies	2026-01-27 01:22:18.047258+02
144ce401-b6ec-4f14-97a2-624933d3b247	t	LAB	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.047258+02	200.00	Hormone Panel	2026-01-27 01:22:18.047258+02
5511d442-75b2-4a5c-8c70-b331500e5478	t	LAB	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.047258+02	300.00	Tumor Markers	2026-01-27 01:22:18.047258+02
4065c318-7364-47da-9131-c60ee8b886ac	t	LAB	100	NOT_COVERED	2026-01-27 01:22:18.047258+02	500.00	Genetic Testing	2026-01-27 01:22:18.047258+02
09ca3686-5e91-48a0-bb0c-0f0f3601b0f1	t	LAB	100	NOT_COVERED	2026-01-27 01:22:18.047875+02	400.00	Allergy Panel (Full)	2026-01-27 01:22:18.047875+02
6d393dc3-8df4-492f-88ae-08065c62c224	t	RADIOLOGY	100	COVERED	2026-01-27 01:22:18.05985+02	80.00	Chest X-Ray	2026-01-27 01:22:18.05985+02
409ce9ea-31b7-46c7-beac-b954d39564f9	t	RADIOLOGY	100	COVERED	2026-01-27 01:22:18.05985+02	100.00	X-Ray Spine (Cervical)	2026-01-27 01:22:18.05985+02
01fcc221-49da-4f07-b1fa-3e323d260d70	t	RADIOLOGY	100	COVERED	2026-01-27 01:22:18.060848+02	100.00	X-Ray Spine (Lumbar)	2026-01-27 01:22:18.060848+02
ddb37e28-411b-4ce7-9e09-784f0ce7b781	t	RADIOLOGY	100	COVERED	2026-01-27 01:22:18.060848+02	70.00	X-Ray Extremities	2026-01-27 01:22:18.060848+02
707994f5-78d9-45d2-9a42-6e5b0fbf9a11	t	RADIOLOGY	100	COVERED	2026-01-27 01:22:18.060848+02	150.00	Ultrasound Abdomen	2026-01-27 01:22:18.060848+02
b2b9385c-6c2b-47df-aed5-a76e0bb9b2b3	t	RADIOLOGY	100	COVERED	2026-01-27 01:22:18.060848+02	150.00	Ultrasound Pelvis	2026-01-27 01:22:18.060848+02
2fbafb90-5167-4b7a-8cd2-1c183b6c2016	t	RADIOLOGY	100	COVERED	2026-01-27 01:22:18.060848+02	200.00	Echocardiogram	2026-01-27 01:22:18.060848+02
96f25e30-3aec-4f13-9983-589364934735	t	RADIOLOGY	100	COVERED	2026-01-27 01:22:18.060848+02	50.00	ECG (Electrocardiogram)	2026-01-27 01:22:18.060848+02
50231064-0f95-48f1-8793-7d9310e1727d	t	RADIOLOGY	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.060848+02	400.00	CT Scan Head	2026-01-27 01:22:18.060848+02
a6ced3eb-b5f6-41f0-83e5-8e246607fe5e	t	RADIOLOGY	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.060848+02	450.00	CT Scan Chest	2026-01-27 01:22:18.060848+02
03e4e073-dc39-452d-a4f1-1667d105dedf	t	RADIOLOGY	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.060848+02	500.00	CT Scan Abdomen	2026-01-27 01:22:18.060848+02
8147880a-3290-4235-b731-71c497fb265f	t	RADIOLOGY	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.060848+02	800.00	MRI Brain	2026-01-27 01:22:18.060848+02
ce841cd1-e16e-4478-b980-6bba1cf55acf	t	RADIOLOGY	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.060848+02	850.00	MRI Spine	2026-01-27 01:22:18.060848+02
c2aa1a5b-3617-4675-9ea9-8dc0daf7b2c1	t	RADIOLOGY	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.060848+02	700.00	MRI Knee	2026-01-27 01:22:18.060848+02
37cf3f6c-84c0-42cb-9c72-d7aeb992dd8e	t	RADIOLOGY	100	NOT_COVERED	2026-01-27 01:22:18.060848+02	2000.00	PET Scan	2026-01-27 01:22:18.060848+02
469b2416-789b-4b37-9e1b-9cd94c89320b	t	RADIOLOGY	100	NOT_COVERED	2026-01-27 01:22:18.060848+02	2500.00	Whole Body MRI	2026-01-27 01:22:18.060848+02
\.


--
-- Data for Name: medicine_prices; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.medicine_prices (id, active, composition, coverage_percentage, coverage_status, created_at, drug_name, generic_name, price, type, unit, updated_at) FROM stdin;
c1ea6c9f-5b3b-4e33-a849-5a131ba686a7	t	\N	100	COVERED	2026-01-27 01:22:18.041848+02	Paracetamol 500mg	Paracetamol	5.00	Tablet	500mg	2026-01-27 01:22:18.041848+02
f8fe3448-4f39-42b8-95b5-fb0fb1a2e2cd	t	\N	100	COVERED	2026-01-27 01:22:18.041848+02	Amoxicillin 500mg	Amoxicillin Trihydrate	15.00	Capsule	500mg	2026-01-27 01:22:18.041848+02
ec1725b9-24b5-4c2d-acb0-8b994f68187b	t	\N	100	COVERED	2026-01-27 01:22:18.041848+02	Ibuprofen 400mg	Ibuprofen	8.00	Tablet	400mg	2026-01-27 01:22:18.041848+02
6ad30801-7df2-4d58-b5f9-45c59cf229fc	t	\N	100	COVERED	2026-01-27 01:22:18.041848+02	Omeprazole 20mg	Omeprazole	12.00	Capsule	20mg	2026-01-27 01:22:18.041848+02
c02002e4-6e56-4ab3-819d-9db6bf47f606	t	\N	100	COVERED	2026-01-27 01:22:18.041848+02	Metformin 500mg	Metformin HCl	10.00	Tablet	500mg	2026-01-27 01:22:18.041848+02
9c87404a-5033-4843-9a0f-94c0b3f8a67f	t	\N	100	COVERED	2026-01-27 01:22:18.042242+02	Amlodipine 5mg	Amlodipine Besylate	15.00	Tablet	5mg	2026-01-27 01:22:18.042242+02
a953b13a-5982-4074-b4b3-1bb85357e4d9	t	\N	100	COVERED	2026-01-27 01:22:18.04227+02	Azithromycin 250mg	Azithromycin	25.00	Tablet	250mg	2026-01-27 01:22:18.04227+02
8d752e35-af7c-40ee-aaea-a92b07a0801a	t	\N	100	COVERED	2026-01-27 01:22:18.04227+02	Cetirizine 10mg	Cetirizine HCl	6.00	Tablet	10mg	2026-01-27 01:22:18.04227+02
aea0c0ec-e4e0-4272-93de-e7081b05bad8	t	\N	100	COVERED	2026-01-27 01:22:18.04227+02	Ranitidine 150mg	Ranitidine HCl	8.00	Tablet	150mg	2026-01-27 01:22:18.04227+02
0a132861-308c-4a1d-a96f-2a7224cdb44d	t	\N	100	COVERED	2026-01-27 01:22:18.04227+02	Metoprolol 50mg	Metoprolol Tartrate	12.00	Tablet	50mg	2026-01-27 01:22:18.04227+02
ceea5fa9-6de0-46af-ba87-787d80b187c4	t	\N	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.04227+02	Atorvastatin 20mg	Atorvastatin Calcium	35.00	Tablet	20mg	2026-01-27 01:22:18.04227+02
a0588a3a-9083-4005-a64a-d3c28e2ddc10	t	\N	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.04227+02	Clopidogrel 75mg	Clopidogrel Bisulfate	45.00	Tablet	75mg	2026-01-27 01:22:18.04227+02
fdfc40b9-ae9f-4f02-bf17-1b86734d8a44	t	\N	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.04227+02	Pregabalin 75mg	Pregabalin	55.00	Capsule	75mg	2026-01-27 01:22:18.04227+02
2d5451f8-719b-468a-b420-5b0bc4555667	t	\N	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.04227+02	Duloxetine 30mg	Duloxetine HCl	65.00	Capsule	30mg	2026-01-27 01:22:18.04227+02
67a0d29f-1680-468b-a2f0-98edc946c2a8	t	\N	100	REQUIRES_APPROVAL	2026-01-27 01:22:18.04227+02	Rosuvastatin 10mg	Rosuvastatin Calcium	40.00	Tablet	10mg	2026-01-27 01:22:18.04227+02
8bcb473d-79a3-488f-ab30-847e5d837a60	t	\N	100	NOT_COVERED	2026-01-27 01:22:18.04227+02	Sildenafil 50mg	Sildenafil Citrate	80.00	Tablet	50mg	2026-01-27 01:22:18.04227+02
44787dad-0c4f-43eb-9ce6-6400eae0def5	t	\N	100	NOT_COVERED	2026-01-27 01:22:18.04227+02	Minoxidil 5%	Minoxidil	120.00	Solution	60ml	2026-01-27 01:22:18.04227+02
d83fb3e2-3591-4396-842f-0126539824c0	t	\N	100	NOT_COVERED	2026-01-27 01:22:18.04227+02	Finasteride 1mg	Finasteride	150.00	Tablet	1mg	2026-01-27 01:22:18.04227+02
\.


--
-- Data for Name: messages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.messages (id, content, is_read, sent_at, conversation_id, receiver_id, sender_id) FROM stdin;
\.


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notifications (id, created_at, message, is_read, replied, type, recipient_id, sender_id) FROM stdin;
40ace575-8e13-4e9c-926d-7ba9ddf262dc	2026-01-27 05:31:28.508063+02	❌ تم رفض طلب إنشاء البروفايل الخاص بك. السبب: {"reason":"a"}	f	f	SYSTEM	95557a6b-92c4-4913-a9c0-8e3c35c67aad	619463cd-6861-4f31-aa6e-eca589183af5
39212a18-b1f1-4870-8119-5522ad347694	2026-01-27 05:31:31.990112+02	❌ تم رفض طلب إنشاء البروفايل الخاص بك. السبب: {"reason":"a"}	f	f	SYSTEM	584a36c8-794c-42c6-9c09-e768dc32ee30	619463cd-6861-4f31-aa6e-eca589183af5
ea8f20a3-a025-48ad-be1a-997fd58dcfce	2026-01-27 05:31:32.001807+02	❌ تم رفض طلب إنشاء البروفايل الخاص بك. السبب: {"reason":"a"}	f	f	SYSTEM	584a36c8-794c-42c6-9c09-e768dc32ee30	619463cd-6861-4f31-aa6e-eca589183af5
831b2195-5084-41cc-85a7-f245e64c17e9	2026-01-27 05:31:35.720276+02	❌ تم رفض طلب إنشاء البروفايل الخاص بك. السبب: {"reason":"a"}	f	f	SYSTEM	4f789b0d-9385-485e-b9c7-a586af5cabd1	619463cd-6861-4f31-aa6e-eca589183af5
bcf59ec7-d304-4f3c-aabc-c7fc0c30d761	2026-01-27 05:27:35.079479+02	مستخدم جديد (جاد جاد جاد) سجل وينتظر الموافقة.	t	f	SYSTEM	619463cd-6861-4f31-aa6e-eca589183af5	619463cd-6861-4f31-aa6e-eca589183af5
b8b54d11-5807-4f08-8006-bd09d3f9da42	2026-01-27 05:39:17.905866+02	📋 لديك طلب فحص إشعاعي جديد من الدكتور د. فاطمة علي الأطفال للمريض محمد أحمد عبدالله	f	f	SYSTEM	48238880-3a07-4d63-8c1f-e7f5fc54da3b	619463cd-6861-4f31-aa6e-eca589183af5
eb6ad9f5-97e2-467f-a86f-8ede1f306880	2026-01-27 05:39:17.908549+02	📋 لديك طلب فحص إشعاعي جديد من الدكتور د. فاطمة علي الأطفال للمريض محمد أحمد عبدالله	f	f	SYSTEM	d2b04366-b548-40a8-957a-1adce0b9173e	619463cd-6861-4f31-aa6e-eca589183af5
da7213da-3011-465d-ab68-d2272cf00681	2026-01-27 05:39:17.908549+02	📊 تم إنشاء طلب فحص إشعاعي جديد بواسطة الدكتور د. فاطمة علي الأطفال - الفحص: إيكو القلب	f	f	SYSTEM	20f27a3d-4257-438d-a327-ae327c0b29ce	619463cd-6861-4f31-aa6e-eca589183af5
b2f6b3d3-0340-49db-a9ce-1c9352bdf17c	2026-01-27 05:39:17.908549+02	New lab test request created by Dr. د. فاطمة علي الأطفال - Test: فحص البول الكامل	f	f	SYSTEM	20f27a3d-4257-438d-a327-ae327c0b29ce	619463cd-6861-4f31-aa6e-eca589183af5
dfc7161b-6e18-486a-87cb-80f65dc1bbba	2026-01-27 05:39:17.91836+02	New lab test request from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله - Test: فحص البول الكامل	f	f	SYSTEM	4f789b0d-9385-485e-b9c7-a586af5cabd1	619463cd-6861-4f31-aa6e-eca589183af5
afd90e1d-4381-4b54-8d0f-d64dfebb04e9	2026-01-27 05:39:17.91836+02	New lab test request from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله - Test: فحص البول الكامل	f	f	SYSTEM	0760c6f6-4000-4aba-8357-86ef3e8f392a	619463cd-6861-4f31-aa6e-eca589183af5
5d0c0d64-402d-4712-98ca-31dbbb69d557	2026-01-27 05:39:17.91836+02	New lab test request from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله - Test: فحص البول الكامل	f	f	SYSTEM	b8b6de49-c402-4c8b-8320-5527e573992b	619463cd-6861-4f31-aa6e-eca589183af5
c5859945-4291-4130-a475-c30a44ca39ed	2026-01-27 05:39:29.769495+02	📋 لديك طلب فحص إشعاعي جديد من الدكتور د. فاطمة علي الأطفال للمريض محمد أحمد عبدالله	f	f	SYSTEM	48238880-3a07-4d63-8c1f-e7f5fc54da3b	619463cd-6861-4f31-aa6e-eca589183af5
36665b9d-0264-471b-9f96-69505dbd0f92	2026-01-27 05:39:29.772489+02	📋 لديك طلب فحص إشعاعي جديد من الدكتور د. فاطمة علي الأطفال للمريض محمد أحمد عبدالله	f	f	SYSTEM	d2b04366-b548-40a8-957a-1adce0b9173e	619463cd-6861-4f31-aa6e-eca589183af5
1cd68085-c827-4be4-b298-1d28ad466492	2026-01-27 05:39:29.774482+02	📊 تم إنشاء طلب فحص إشعاعي جديد بواسطة الدكتور د. فاطمة علي الأطفال - الفحص: إيكو القلب	f	f	SYSTEM	20f27a3d-4257-438d-a327-ae327c0b29ce	619463cd-6861-4f31-aa6e-eca589183af5
c1785f3b-1dd8-4ed0-a072-66aad21b089f	2026-01-27 05:39:29.770493+02	New lab test request created by Dr. د. فاطمة علي الأطفال - Test: فحص البول الكامل	f	f	SYSTEM	20f27a3d-4257-438d-a327-ae327c0b29ce	619463cd-6861-4f31-aa6e-eca589183af5
fc41d702-92ee-4ef4-a59f-3bc98d65db3a	2026-01-27 05:39:29.774482+02	New lab test request from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله - Test: فحص البول الكامل	f	f	SYSTEM	4f789b0d-9385-485e-b9c7-a586af5cabd1	619463cd-6861-4f31-aa6e-eca589183af5
01c23151-0cf2-48f7-ad87-208a5f695a4e	2026-01-27 05:39:29.776477+02	New lab test request from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله - Test: فحص البول الكامل	f	f	SYSTEM	0760c6f6-4000-4aba-8357-86ef3e8f392a	619463cd-6861-4f31-aa6e-eca589183af5
c38d7161-b614-47da-80fc-2654eb82043b	2026-01-27 05:39:29.778068+02	New lab test request from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله - Test: فحص البول الكامل	f	f	SYSTEM	b8b6de49-c402-4c8b-8320-5527e573992b	619463cd-6861-4f31-aa6e-eca589183af5
494c9350-556a-4ae8-ab26-5d96e3dcb054	2026-01-27 05:39:29.782062+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله	f	f	SYSTEM	584a36c8-794c-42c6-9c09-e768dc32ee30	619463cd-6861-4f31-aa6e-eca589183af5
1257393c-96d1-4206-83e1-eacdeb2c67ee	2026-01-27 05:39:29.784054+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله	f	f	SYSTEM	01f4d329-987d-4560-98bf-298a3ece3f27	619463cd-6861-4f31-aa6e-eca589183af5
547e75fe-3983-4c5d-ad7c-d5893fdca995	2026-01-27 05:39:29.786048+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله	f	f	SYSTEM	8f491018-1a64-4a6f-b9d6-c19e2f2e7c7c	619463cd-6861-4f31-aa6e-eca589183af5
97afb2c9-0883-42c1-b0db-a3b1e284b69f	2026-01-27 05:39:29.788043+02	💊 New prescription created for you by Dr. د. فاطمة علي الأطفال	f	f	SYSTEM	20f27a3d-4257-438d-a327-ae327c0b29ce	619463cd-6861-4f31-aa6e-eca589183af5
4e97c70d-7468-470a-8693-460b3fce774e	2026-01-27 05:41:07.800189+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله	f	f	SYSTEM	584a36c8-794c-42c6-9c09-e768dc32ee30	619463cd-6861-4f31-aa6e-eca589183af5
8be92401-bd18-4b47-a05a-3fb9ea5652b6	2026-01-27 05:41:07.802651+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله	f	f	SYSTEM	01f4d329-987d-4560-98bf-298a3ece3f27	619463cd-6861-4f31-aa6e-eca589183af5
826ec6b0-24bf-4a36-8514-e751e09f280b	2026-01-27 05:41:07.802838+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله	f	f	SYSTEM	8f491018-1a64-4a6f-b9d6-c19e2f2e7c7c	619463cd-6861-4f31-aa6e-eca589183af5
4e199c0f-84ff-4c6c-a93f-1dc25ace91c9	2026-01-27 05:41:07.802838+02	💊 New prescription created for you by Dr. د. فاطمة علي الأطفال	f	f	SYSTEM	20f27a3d-4257-438d-a327-ae327c0b29ce	619463cd-6861-4f31-aa6e-eca589183af5
ad4aeac4-6f0f-4c51-a93f-a6e76ce9131f	2026-01-27 05:49:21.04075+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله	f	f	SYSTEM	584a36c8-794c-42c6-9c09-e768dc32ee30	619463cd-6861-4f31-aa6e-eca589183af5
7801d56b-9dcf-4fcb-a2d4-a03af1c31f30	2026-01-27 05:49:21.043748+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله	f	f	SYSTEM	01f4d329-987d-4560-98bf-298a3ece3f27	619463cd-6861-4f31-aa6e-eca589183af5
178fd1e6-fa9f-4c3e-9bc0-5681860ba3f8	2026-01-27 05:49:21.045737+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله	f	f	SYSTEM	8f491018-1a64-4a6f-b9d6-c19e2f2e7c7c	619463cd-6861-4f31-aa6e-eca589183af5
1f68e101-ce82-4e7c-bd94-ee905571a3ff	2026-01-27 05:49:21.047732+02	💊 New prescription created for you by Dr. د. فاطمة علي الأطفال	f	f	SYSTEM	20f27a3d-4257-438d-a327-ae327c0b29ce	619463cd-6861-4f31-aa6e-eca589183af5
4583266f-1ed1-4418-b3c8-3899ddb80000	2026-01-27 05:55:09.334107+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله	f	f	SYSTEM	584a36c8-794c-42c6-9c09-e768dc32ee30	619463cd-6861-4f31-aa6e-eca589183af5
ae90e312-2ef1-45a9-a8da-9253d141910c	2026-01-27 05:55:09.34009+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله	f	f	SYSTEM	01f4d329-987d-4560-98bf-298a3ece3f27	619463cd-6861-4f31-aa6e-eca589183af5
e41396e3-e82f-49be-a98d-d961d6025afb	2026-01-27 05:55:09.344077+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for patient محمد أحمد عبدالله	f	f	SYSTEM	8f491018-1a64-4a6f-b9d6-c19e2f2e7c7c	619463cd-6861-4f31-aa6e-eca589183af5
0fd1ec71-7d9a-4df2-8242-804f3bb4540f	2026-01-27 05:55:09.348067+02	💊 New prescription created for you by Dr. د. فاطمة علي الأطفال	f	f	SYSTEM	20f27a3d-4257-438d-a327-ae327c0b29ce	619463cd-6861-4f31-aa6e-eca589183af5
8fe1caa1-e02d-46ca-b586-315cbbe899da	2026-01-27 05:59:31.846146+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for family member يوسف محمد أحمد (SON) - Client: محمد أحمد عبدالله	f	f	SYSTEM	584a36c8-794c-42c6-9c09-e768dc32ee30	619463cd-6861-4f31-aa6e-eca589183af5
4dde3811-263c-4d37-9c2f-7d8aa7e0c68c	2026-01-27 05:59:31.84814+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for family member يوسف محمد أحمد (SON) - Client: محمد أحمد عبدالله	f	f	SYSTEM	01f4d329-987d-4560-98bf-298a3ece3f27	619463cd-6861-4f31-aa6e-eca589183af5
a1d55a5b-2ad9-450a-8cd8-a39318d62c50	2026-01-27 05:59:31.851133+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for family member يوسف محمد أحمد (SON) - Client: محمد أحمد عبدالله	f	f	SYSTEM	8f491018-1a64-4a6f-b9d6-c19e2f2e7c7c	619463cd-6861-4f31-aa6e-eca589183af5
9c0c4bd8-3e61-47c9-9a4e-f2252351c40c	2026-01-27 05:59:31.853128+02	💊 New prescription created by Dr. د. فاطمة علي الأطفال for family member يوسف محمد أحمد (SON)	f	f	SYSTEM	20f27a3d-4257-438d-a327-ae327c0b29ce	619463cd-6861-4f31-aa6e-eca589183af5
a1c41bdc-3625-4e4b-8b0f-243318805651	2026-01-27 06:07:44.882839+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for family member مريم محمد أحمد (DAUGHTER) - Client: محمد أحمد عبدالله	f	f	SYSTEM	584a36c8-794c-42c6-9c09-e768dc32ee30	619463cd-6861-4f31-aa6e-eca589183af5
67931e9b-b1c4-4ac0-bc3b-adf0c9677327	2026-01-27 06:07:44.884835+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for family member مريم محمد أحمد (DAUGHTER) - Client: محمد أحمد عبدالله	f	f	SYSTEM	01f4d329-987d-4560-98bf-298a3ece3f27	619463cd-6861-4f31-aa6e-eca589183af5
c3541b98-d2de-42ba-b6fb-62fece69cedd	2026-01-27 06:07:44.88683+02	📋 New prescription from Dr. د. فاطمة علي الأطفال for family member مريم محمد أحمد (DAUGHTER) - Client: محمد أحمد عبدالله	f	f	SYSTEM	8f491018-1a64-4a6f-b9d6-c19e2f2e7c7c	619463cd-6861-4f31-aa6e-eca589183af5
8ff4f78d-948d-4c4e-bcd3-2bdba0b81ba6	2026-01-27 06:07:44.887827+02	💊 New prescription created by Dr. د. فاطمة علي الأطفال for family member مريم محمد أحمد (DAUGHTER)	f	f	SYSTEM	20f27a3d-4257-438d-a327-ae327c0b29ce	619463cd-6861-4f31-aa6e-eca589183af5
667d28b4-9974-40f4-81cb-a4fe8363d15c	2026-01-27 06:07:45.164979+02	📋 تم إنشاء مطالبة طبية لك من د. فاطمة علي الأطفال - المبلغ: 80.0 شيكل - في انتظار المراجعة	f	f	SYSTEM	20f27a3d-4257-438d-a327-ae327c0b29ce	619463cd-6861-4f31-aa6e-eca589183af5
ce462507-5439-4b17-828f-2b5d5924cc46	2026-01-27 06:07:45.160989+02	✅ تم إرسال مطالبتك بنجاح - المبلغ: 80.0 شيكل للمريض مريم محمد أحمد - في انتظار المراجعة الطبية	t	f	SYSTEM	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	619463cd-6861-4f31-aa6e-eca589183af5
962cc1c2-2dd8-4ad1-a718-ae9310947d50	2026-01-27 06:07:45.156002+02	📋 مطالبة جديدة من د. فاطمة علي الأطفال للمريض مريم محمد أحمد - المبلغ: 80.0 شيكل	t	f	SYSTEM	5c5b1b6c-ca8a-4083-a3d9-8f40022a5cd5	619463cd-6861-4f31-aa6e-eca589183af5
65c61555-a3fb-4d09-aafd-a2fa19d19c11	2026-01-27 06:30:55.568221+02	✅ تمت الموافقة على وصفتك من الصيدلي يوسف أحمد الصيدلي - المجموع: 18.666666666666664 ₪	f	f	SYSTEM	20f27a3d-4257-438d-a327-ae327c0b29ce	619463cd-6861-4f31-aa6e-eca589183af5
437ffe80-31d5-4f59-943d-0b0333ce8983	2026-01-27 06:30:55.570216+02	✅ تمت الموافقة على وصفتك للمريض محمد أحمد عبدالله من الصيدلي يوسف أحمد الصيدلي	f	f	SYSTEM	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	619463cd-6861-4f31-aa6e-eca589183af5
\.


--
-- Data for Name: password_reset_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.password_reset_tokens (id, created_at, expires_at, token, used, username) FROM stdin;
\.


--
-- Data for Name: policies; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.policies (id, coverage_limit, created_at, deductible, description, emergency_rules, end_date, name, policy_no, start_date, status, updated_at) FROM stdin;
11aea7b1-5ee0-43a0-a746-52006c165769	100000.00	2026-01-27 01:22:14.01373+02	50.00	تغطية شاملة لجميع الخدمات الطبية بما في ذلك الأسنان والنظارات	تغطية طوارئ كاملة على مدار الساعة	2025-12-31	التأمين الذهبي الشامل	POL-GOLD-2024	2024-01-01	ACTIVE	2026-01-27 01:22:14.01373+02
9d7e59f0-6801-41a4-905a-23bf1ab663e1	50000.00	2026-01-27 01:22:14.034904+02	100.00	تغطية جيدة للخدمات الطبية الأساسية	تغطية طوارئ محدودة	2025-12-31	التأمين الفضي	POL-SILVER-2024	2024-01-01	ACTIVE	2026-01-27 01:22:14.034904+02
6322e814-3596-41a9-9943-a31db4b2813e	25000.00	2026-01-27 01:22:14.044859+02	200.00	تغطية أساسية للخدمات الطبية الضرورية	طوارئ فقط للحالات الحرجة	2025-12-31	التأمين البرونزي الأساسي	POL-BRONZE-2024	2024-01-01	ACTIVE	2026-01-27 01:22:14.044859+02
\.


--
-- Data for Name: prescription_items; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.prescription_items (id, calculated_quantity, covered_quantity, created_at, dispensed_quantity, dosage, drug_form, duration, expiry_date, final_price, pharmacist_price, pharmacist_price_per_unit, times_per_day, union_price_per_unit, updated_at, prescription_id, price_list_id) FROM stdin;
6b78aa4b-769e-4828-b97f-69841423ae96	6	\N	2026-01-27 05:41:07.759072+02	\N	1	\N	3	2026-01-30 05:41:07.759072+02	\N	\N	\N	2	40	2026-01-27 05:41:07.759072+02	e3509d47-df24-4e4d-8022-8c3c7218c5fa	9ed4c52d-b0e1-49b6-81d2-2aeaf1387f8c
8f04aaf8-95e1-417e-90d6-4897933fcb8b	6	\N	2026-01-27 05:49:21.014819+02	\N	1	\N	3	2026-01-30 05:49:21.013822+02	\N	\N	\N	2	1.6666666666666667	2026-01-27 05:49:21.014819+02	2fbd7f72-5f97-457d-9bf0-707c37f09c09	131d9127-6b7b-416a-abec-6b492b376cf4
1aeb5e51-e0be-4770-85f6-28503c38ad91	6	\N	2026-01-27 05:55:09.302192+02	\N	1	\N	3	2026-01-30 05:55:09.302192+02	\N	\N	\N	2	0.4166666666666667	2026-01-27 05:55:09.302192+02	fd6e8670-864b-4b27-8c6c-0c866eb3ad9d	8691138d-e0ae-44c9-89ef-3c19a9ededf8
2875fda7-3065-4264-918d-60470eb0a379	66	\N	2026-01-27 05:59:31.821213+02	\N	1	\N	33	2026-03-01 05:59:31.821213+02	\N	\N	\N	2	1.3333333333333333	2026-01-27 05:59:31.821213+02	e2ee2742-9a51-4149-83ee-d69a5df69f90	5de14aa3-75cd-4567-bc70-a8d82f6350a2
c6ad32e0-0e4d-42c5-81dc-6a34e95ff5a9	6	\N	2026-01-27 06:07:44.860898+02	\N	1	\N	3	2026-01-30 06:07:44.860898+02	\N	\N	\N	2	1.3333333333333333	2026-01-27 06:07:44.860898+02	51a60673-9f5d-442b-920a-cac162e41cb9	5de14aa3-75cd-4567-bc70-a8d82f6350a2
a6e3736e-a902-4d28-ae5a-50ff441c3911	14	14	2026-01-27 05:39:29.754233+02	14	1	\N	7	2026-02-03 05:39:29.754233+02	18.666666666666664	200	14.285714285714286	2	1.3333333333333333	2026-01-27 06:30:55.544286+02	7c127c26-4f45-47d0-b48c-79df168ba476	5de14aa3-75cd-4567-bc70-a8d82f6350a2
\.


--
-- Data for Name: prescriptions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.prescriptions (id, created_at, diagnosis, is_chronic, status, total_price, treatment, updated_at, doctor_id, member_id, pharmacist_id) FROM stdin;
e3509d47-df24-4e4d-8022-8c3c7218c5fa	2026-01-27 05:41:07.75772+02	التهاب الأذن	f	PENDING	0	خافض حرارة	2026-01-27 05:41:07.75772+02	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	20f27a3d-4257-438d-a327-ae327c0b29ce	\N
2fbd7f72-5f97-457d-9bf0-707c37f09c09	2026-01-27 05:49:21.01083+02	الإسهال, الحمى, التهاب الأذن, التهاب الحلق, EMP011	f	PENDING	0	dsfsdfsdfsdfsd	2026-01-27 05:49:21.01083+02	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	20f27a3d-4257-438d-a327-ae327c0b29ce	\N
fd6e8670-864b-4b27-8c6c-0c866eb3ad9d	2026-01-27 05:55:09.298201+02	التهاب الحلق	f	PENDING	0	kj	2026-01-27 05:55:09.298201+02	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	20f27a3d-4257-438d-a327-ae327c0b29ce	\N
e2ee2742-9a51-4149-83ee-d69a5df69f90	2026-01-27 05:59:31.820216+02	التهاب الأذن	f	PENDING	0	asdasdasdas\nFamily Member: يوسف محمد أحمد (SON) - Insurance: EMP6000000001.02 - Age: 10 years - Gender: MALE	2026-01-27 05:59:31.820216+02	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	20f27a3d-4257-438d-a327-ae327c0b29ce	\N
51a60673-9f5d-442b-920a-cac162e41cb9	2026-01-27 06:07:44.859901+02	التهاب الحلق	f	PENDING	0	dsafas\nFamily Member: مريم محمد أحمد (DAUGHTER) - Insurance: EMP6000000001.03 - Age: 7 years - Gender: FEMALE	2026-01-27 06:07:44.859901+02	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	20f27a3d-4257-438d-a327-ae327c0b29ce	\N
7c127c26-4f45-47d0-b48c-79df168ba476	2026-01-27 05:39:29.753236+02	الحمى	f	VERIFIED	18.666666666666664	خافض حرارة	2026-01-27 06:30:55.544286+02	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	20f27a3d-4257-438d-a327-ae327c0b29ce	584a36c8-794c-42c6-9c09-e768dc32ee30
\.


--
-- Data for Name: price_list; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.price_list (id, active, coverage_percentage, coverage_status, created_at, max_age, min_age, notes, price, provider_type, quantity, service_code, service_details, service_name, updated_at) FROM stdin;
e35a67b9-99df-4a31-9fb1-340e5968e6c9	t	80	COVERED	2026-01-27 01:22:14.053865+02	\N	\N	مسكن للألم وخافض للحرارة	15	PHARMACY	30	MED001	\N	باراسيتامول 500mg	2026-01-27 01:22:14.053865+02
1c5aeb32-c465-492d-8505-0bcbacdc10ea	t	80	COVERED	2026-01-27 01:22:14.055859+02	\N	\N	مضاد حيوي واسع المجال	25	PHARMACY	21	MED002	\N	أموكسيسيلين 500mg	2026-01-27 01:22:14.055859+02
96a66665-45f6-4132-a402-f836c1af61fd	t	80	COVERED	2026-01-27 01:22:14.056857+02	\N	\N	لعلاج حموضة المعدة	35	PHARMACY	28	MED003	\N	أوميبرازول 20mg	2026-01-27 01:22:14.056857+02
31b8c2e8-c1cb-4d3f-8dad-41477a081089	t	80	COVERED	2026-01-27 01:22:14.057853+02	\N	\N	لعلاج السكري من النوع الثاني	20	PHARMACY	30	MED004	\N	ميتفورمين 850mg	2026-01-27 01:22:14.057853+02
fdd21a26-7ffa-4994-a275-89f61252c239	t	80	COVERED	2026-01-27 01:22:14.058851+02	\N	\N	لعلاج ارتفاع ضغط الدم	30	PHARMACY	30	MED005	\N	أملوديبين 5mg	2026-01-27 01:22:14.058851+02
3dab6092-13de-4a15-9eac-e173167e504c	t	80	COVERED	2026-01-27 01:22:14.059849+02	\N	\N	مضاد للحساسية	18	PHARMACY	20	MED006	\N	سيتريزين 10mg	2026-01-27 01:22:14.059849+02
c613f829-45ae-4fdf-8fae-973e2fa5f8c0	t	80	COVERED	2026-01-27 01:22:14.060845+02	\N	\N	مسكن ومضاد للالتهاب	22	PHARMACY	30	MED007	\N	إيبوبروفين 400mg	2026-01-27 01:22:14.060845+02
8bbe514a-19f6-4adb-a7d3-7c16462aeecd	t	80	COVERED	2026-01-27 01:22:14.061842+02	\N	\N	مضاد حيوي	45	PHARMACY	6	MED008	\N	أزيثرومايسين 250mg	2026-01-27 01:22:14.061842+02
5de14aa3-75cd-4567-bc70-a8d82f6350a2	t	80	COVERED	2026-01-27 01:22:14.062988+02	\N	\N	لعلاج ضغط الدم	40	PHARMACY	30	MED009	\N	لوسارتان 50mg	2026-01-27 01:22:14.062988+02
131d9127-6b7b-416a-abec-6b492b376cf4	t	80	COVERED	2026-01-27 01:22:14.063986+02	\N	\N	لخفض الكوليسترول	50	PHARMACY	30	MED010	\N	أتورفاستاتين 20mg	2026-01-27 01:22:14.063986+02
8691138d-e0ae-44c9-89ef-3c19a9ededf8	t	80	COVERED	2026-01-27 01:22:14.065013+02	\N	\N	مكمل غذائي	25	PHARMACY	60	MED011	\N	فيتامين د 1000 وحدة	2026-01-27 01:22:14.065013+02
140bb1ca-4e77-4355-b006-8d199e38923a	t	80	COVERED	2026-01-27 01:22:14.06598+02	\N	\N	مكمل غذائي للقلب	35	PHARMACY	90	MED012	\N	أوميغا 3	2026-01-27 01:22:14.06598+02
9ed4c52d-b0e1-49b6-81d2-2aeaf1387f8c	t	80	COVERED	2026-01-27 01:22:14.066977+02	\N	\N	موسع للشعب الهوائية	40	PHARMACY	1	MED013	\N	فنتولين بخاخ	2026-01-27 01:22:14.066977+02
ade1e682-7ab5-4f25-b345-52c701a95cfe	t	80	COVERED	2026-01-27 01:22:14.067976+02	\N	\N	لعلاج السكري	120	PHARMACY	5	MED014	\N	انسولين نوفورابيد	2026-01-27 01:22:14.067976+02
27e3a702-9851-416e-b035-5766e634341a	t	80	COVERED	2026-01-27 01:22:14.068982+02	\N	\N	مضاد حيوي موضعي	28	PHARMACY	1	MED015	\N	كريم فيوسيدين	2026-01-27 01:22:14.068982+02
651f07e9-eb29-4125-82e0-2f6fbd2b9bd7	t	90	COVERED	2026-01-27 01:22:14.070994+02	\N	\N	فحص مكونات الدم	50	LAB	\N	LAB001	\N	فحص دم شامل CBC	2026-01-27 01:22:14.070994+02
8c48d0a6-0484-4e5e-a3c0-c03004453c0a	t	90	COVERED	2026-01-27 01:22:14.071969+02	\N	\N	قياس مستوى السكر	25	LAB	\N	LAB002	\N	فحص السكر صائم	2026-01-27 01:22:14.071969+02
0286a2dc-c244-4de1-a8b2-a2c9ede23795	t	90	COVERED	2026-01-27 01:22:14.07299+02	\N	\N	متوسط السكر 3 أشهر	60	LAB	\N	LAB003	\N	فحص السكر التراكمي HbA1c	2026-01-27 01:22:14.07299+02
30a466b5-596e-413c-99fe-30bafc704491	t	90	COVERED	2026-01-27 01:22:14.07299+02	\N	\N	كرياتينين ويوريا	80	LAB	\N	LAB004	\N	فحص وظائف الكلى	2026-01-27 01:22:14.07299+02
c3543ce4-27ff-48b2-8ea5-c6ca24a3750b	t	90	COVERED	2026-01-27 01:22:14.07299+02	\N	\N	إنزيمات الكبد	90	LAB	\N	LAB005	\N	فحص وظائف الكبد	2026-01-27 01:22:14.07299+02
287ceaad-a26a-4ffa-9e39-17dbd1537f3d	t	90	COVERED	2026-01-27 01:22:14.07299+02	\N	\N	كوليسترول ودهون ثلاثية	70	LAB	\N	LAB006	\N	فحص الدهون الشامل	2026-01-27 01:22:14.07299+02
edef0f11-9b1e-48b8-a9b3-d37ba1ef0f68	t	90	COVERED	2026-01-27 01:22:14.07299+02	\N	\N	هرمون الغدة الدرقية	65	LAB	\N	LAB007	\N	فحص الغدة الدرقية TSH	2026-01-27 01:22:14.07299+02
2826741c-6725-416a-8661-db621496cbd9	t	90	COVERED	2026-01-27 01:22:14.07299+02	\N	\N	مستوى فيتامين د	80	LAB	\N	LAB008	\N	فحص فيتامين د	2026-01-27 01:22:14.07299+02
ad1b7629-eebe-4e9b-bd96-6fe0dcdc6962	t	90	COVERED	2026-01-27 01:22:14.07299+02	\N	\N	تحليل البول	30	LAB	\N	LAB009	\N	فحص البول الكامل	2026-01-27 01:22:14.07299+02
656783ad-f2a1-4224-8e3c-b22e58e23f77	t	90	COVERED	2026-01-27 01:22:14.07299+02	\N	\N	كشف هرمون الحمل	35	LAB	\N	LAB010	\N	فحص الحمل	2026-01-27 01:22:14.07299+02
420bb2c8-89ac-48c6-a41e-1a1dd42a6677	t	90	COVERED	2026-01-27 01:22:14.081841+02	\N	\N	للنقرس	40	LAB	\N	LAB011	\N	فحص حمض اليوريك	2026-01-27 01:22:14.081841+02
8841d519-d2d2-4496-8c2f-815aeacbcba2	t	90	COVERED	2026-01-27 01:22:14.082839+02	\N	\N	مخزون الحديد	75	LAB	\N	LAB012	\N	فحص الحديد وفيريتين	2026-01-27 01:22:14.082839+02
e5803df7-0680-4ee3-986f-3fb7f9d1f073	t	90	COVERED	2026-01-27 01:22:14.083836+02	\N	\N	كشف البكتيريا	90	LAB	\N	LAB013	\N	زراعة بول	2026-01-27 01:22:14.083836+02
e45c8175-f20d-4ee5-918b-5d097d8956d9	t	90	COVERED	2026-01-27 01:22:14.084833+02	\N	\N	كوفيد-19	150	LAB	\N	LAB014	\N	فحص فيروس كورونا PCR	2026-01-27 01:22:14.084833+02
ba6cbefb-6c11-40c0-bc53-e10b3310ba80	t	90	COVERED	2026-01-27 01:22:14.085832+02	\N	\N	عامل الروماتويد	55	LAB	\N	LAB015	\N	فحص الروماتيزم RF	2026-01-27 01:22:14.085832+02
905e8717-dca3-4fd8-8247-34188feff164	t	85	COVERED	2026-01-27 01:22:14.086841+02	\N	\N	تصوير الصدر	80	RADIOLOGY	\N	RAD001	\N	أشعة صدر X-Ray	2026-01-27 01:22:14.086841+02
4a283912-b26d-4186-84ce-257d339cad02	t	85	COVERED	2026-01-27 01:22:14.087825+02	\N	\N	تصوير العظام	70	RADIOLOGY	\N	RAD002	\N	أشعة عظام	2026-01-27 01:22:14.087825+02
92444779-b1c3-49b5-9230-193c3ab78ad8	t	85	COVERED	2026-01-27 01:22:14.088823+02	\N	\N	تصوير بالموجات الصوتية	150	RADIOLOGY	\N	RAD003	\N	سونار بطن كامل	2026-01-27 01:22:14.088823+02
46886ec8-fd87-489d-9de0-f06ce537110f	t	85	COVERED	2026-01-27 01:22:14.090819+02	\N	\N	فحص الغدة	120	RADIOLOGY	\N	RAD004	\N	سونار الغدة الدرقية	2026-01-27 01:22:14.090819+02
2ce03ce1-5bcc-4349-b83d-adad87671330	t	85	COVERED	2026-01-27 01:22:14.091803+02	\N	\N	تصوير الدماغ	400	RADIOLOGY	\N	RAD005	\N	أشعة مقطعية CT للرأس	2026-01-27 01:22:14.091803+02
29191b8a-d3ff-49e7-af36-523363f1c94d	t	85	COVERED	2026-01-27 01:22:14.092801+02	\N	\N	تصوير البطن	500	RADIOLOGY	\N	RAD006	\N	أشعة مقطعية CT للبطن	2026-01-27 01:22:14.092801+02
cc054896-abe2-4c59-869f-1b245b916d71	t	85	COVERED	2026-01-27 01:22:14.093799+02	\N	\N	تصوير المفاصل	800	RADIOLOGY	\N	RAD007	\N	رنين مغناطيسي MRI للركبة	2026-01-27 01:22:14.093799+02
7cded0a5-f722-4a5e-a893-10b660589ffd	t	85	COVERED	2026-01-27 01:22:14.094795+02	\N	\N	تصوير العمود الفقري	900	RADIOLOGY	\N	RAD008	\N	رنين مغناطيسي MRI للظهر	2026-01-27 01:22:14.094795+02
93ae51ce-a05b-4692-a5dd-f3e624099385	t	85	COVERED	2026-01-27 01:22:14.094795+02	\N	\N	فحص الثدي	200	RADIOLOGY	\N	RAD009	\N	ماموغرام	2026-01-27 01:22:14.094795+02
40d7c16a-f99d-4776-bd20-a380d8d3c383	t	85	COVERED	2026-01-27 01:22:14.095794+02	\N	\N	تصوير القلب بالموجات	250	RADIOLOGY	\N	RAD010	\N	إيكو القلب	2026-01-27 01:22:14.095794+02
25f206c1-cd06-4511-91f5-fde7b8c783d1	t	85	COVERED	2026-01-27 01:22:14.09679+02	\N	\N	فحص الشرايين والأوردة	300	RADIOLOGY	\N	RAD011	\N	دوبلر الأوعية الدموية	2026-01-27 01:22:14.09679+02
10e13b5c-853b-4912-964c-9c0db8c848f2	t	85	COVERED	2026-01-27 01:22:14.09679+02	\N	\N	تصوير الفكين	100	RADIOLOGY	\N	RAD012	\N	بانوراما الأسنان	2026-01-27 01:22:14.09679+02
\.


--
-- Data for Name: price_list_allowed_genders; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.price_list_allowed_genders (price_list_id, gender) FROM stdin;
\.


--
-- Data for Name: price_list_allowed_specializations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.price_list_allowed_specializations (price_list_id, specialization_id) FROM stdin;
\.


--
-- Data for Name: provider_policies; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.provider_policies (id, active, coverage_percent, created_at, effective_from, effective_to, negotiated_price, service_name, updated_at, provider_id) FROM stdin;
\.


--
-- Data for Name: radiology_requests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.radiology_requests (id, approved_price, created_at, diagnosis, entered_price, notes, result_url, status, test_name, treatment, updated_at, doctor_id, member_id, radiologist_id, price_id) FROM stdin;
af3978e5-a44a-47e6-8c7d-8d7d7aec203b	\N	2026-01-27 05:39:17.880111+02	الحمى	\N	الحمى	\N	PENDING	إيكو القلب	خافض حرارة	2026-01-27 05:39:17.880111+02	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	20f27a3d-4257-438d-a327-ae327c0b29ce	\N	40d7c16a-f99d-4776-bd20-a380d8d3c383
e6b4d6e4-da13-4a26-a555-47f89cb1bcaa	\N	2026-01-27 05:39:29.749247+02	الحمى	\N	الحمى	\N	PENDING	إيكو القلب	خافض حرارة	2026-01-27 05:39:29.749247+02	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	20f27a3d-4257-438d-a327-ae327c0b29ce	\N	40d7c16a-f99d-4776-bd20-a380d8d3c383
\.


--
-- Data for Name: revoked_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.revoked_tokens (id, expires_at, revoked_at, token) FROM stdin;
1	2026-01-28 01:23:23+02	2026-01-27 02:23:37.725159+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYW5hZ2VyQGRlbW8uY29tIiwiaWF0IjoxNzY5NDY5ODAzLCJleHAiOjE3Njk1NTYyMDN9.D9WeHX3bPWASpAhZS4Gfr14bVAjTptSxRr9MNeBG25E
2	2026-01-28 04:46:04+02	2026-01-27 04:48:41.545677+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkb2N0b3IucGVkaWF0cmljQGRlbW8uY29tIiwiaWF0IjoxNzY5NDgxOTY0LCJleHAiOjE3Njk1NjgzNjR9.Mk4w7G_9O4Cb_5y6SHu0uuMm2_36ePaHQqqYCMzQM1g
3	2026-01-28 04:50:26+02	2026-01-27 04:51:26.927348+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjbGllbnQxQGRlbW8uY29tIiwiaWF0IjoxNzY5NDgyMjI2LCJleHAiOjE3Njk1Njg2MjZ9.wkYV2iU0SUaVrDA_kf0qxKVCY_UeKM76PWMwwYKqvAo
4	2026-01-28 05:27:44+02	2026-01-27 05:27:51.596548+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZWRpY2FsQGRlbW8uY29tIiwiaWF0IjoxNzY5NDg0NDY0LCJleHAiOjE3Njk1NzA4NjR9.x9qVECUW5h3qBP_iwRGNjNnlhA2nvRgi6ILjn7Q6frI
5	2026-01-28 05:28:06+02	2026-01-27 05:37:02.048983+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYW5hZ2VyQGRlbW8uY29tIiwiaWF0IjoxNzY5NDg0NDg2LCJleHAiOjE3Njk1NzA4ODZ9.b_DSIcuWf0FguJO-OEmyVoHERhQcDeIOzQdKD-GR0Ao
6	2026-01-28 05:37:24+02	2026-01-27 05:37:51.109499+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYW5hZ2VyQGRlbW8uY29tIiwiaWF0IjoxNzY5NDg1MDQ0LCJleHAiOjE3Njk1NzE0NDR9.UzX0g4zZszoh4nlyw_TAbosRQp3LqmAyKbmjl2sppik
7	2026-01-28 05:38:00+02	2026-01-27 05:38:08.589693+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjbGllbnQxQGRlbW8uY29tIiwiaWF0IjoxNzY5NDg1MDgwLCJleHAiOjE3Njk1NzE0ODB9.LCwXRSwglstfiiIGkUB-KGNLqvoWT92tBdnhpKHm54U
8	2026-01-28 05:38:13+02	2026-01-27 05:58:46.547768+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkb2N0b3IucGVkaWF0cmljQGRlbW8uY29tIiwiaWF0IjoxNzY5NDg1MDkzLCJleHAiOjE3Njk1NzE0OTN9.uiCuze3qfeYtsOq9c0a-30NxjOQ7fDbYfZVJrnIFrtE
9	2026-01-28 05:58:51+02	2026-01-27 05:58:59.936255+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjbGllbnQxQGRlbW8uY29tIiwiaWF0IjoxNzY5NDg2MzMxLCJleHAiOjE3Njk1NzI3MzF9.5CLMfrW-6kw58OuE_0zo091A-UzFBsSzxuExvf0IpKQ
10	2026-01-28 05:59:08+02	2026-01-27 06:00:50.426123+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkb2N0b3IucGVkaWF0cmljQGRlbW8uY29tIiwiaWF0IjoxNzY5NDg2MzQ4LCJleHAiOjE3Njk1NzI3NDh9.BigZttMRwtVPmP2N5kIn-XCaCykB9_GciwiE5usFQWs
11	2026-01-28 06:00:56+02	2026-01-27 06:07:20.907362+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZWRpY2FsQGRlbW8uY29tIiwiaWF0IjoxNzY5NDg2NDU2LCJleHAiOjE3Njk1NzI4NTZ9.mjJZMNkRXCbeTw__7d63XqsMCQFozfi8-ZKelu0SJL0
12	2026-01-28 06:07:25+02	2026-01-27 06:07:59.268547+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkb2N0b3IucGVkaWF0cmljQGRlbW8uY29tIiwiaWF0IjoxNzY5NDg2ODQ1LCJleHAiOjE3Njk1NzMyNDV9.q6ukKrwAwTY6OFX5CULeS2GYZF8evS2rcvmjSoEQfu4
13	2026-01-28 06:08:05+02	2026-01-27 06:09:48.211327+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZWRpY2FsQGRlbW8uY29tIiwiaWF0IjoxNzY5NDg2ODg1LCJleHAiOjE3Njk1NzMyODV9.pQOqlSq4Dbw83ohBLg5K2db6k4Oz6I3BC7GgYIsUHCU
14	2026-01-28 06:09:51+02	2026-01-27 06:16:38.033931+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwaGFybWFjeTFAZGVtby5jb20iLCJpYXQiOjE3Njk0ODY5OTEsImV4cCI6MTc2OTU3MzM5MX0.4KPX4lkvzC6UFn6JeBpM2Wg74IqX_fJVyX9MDveVJMA
15	2026-01-28 06:16:45+02	2026-01-27 06:16:51.735017+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjbGllbnQxQGRlbW8uY29tIiwiaWF0IjoxNzY5NDg3NDA1LCJleHAiOjE3Njk1NzM4MDV9._wNV9oLhFIu1pDrnn7bhB0qOoUXYpXhcdUjXBELPDQE
16	2026-01-28 06:16:57+02	2026-01-27 06:19:14.990996+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwaGFybWFjeTFAZGVtby5jb20iLCJpYXQiOjE3Njk0ODc0MTcsImV4cCI6MTc2OTU3MzgxN30.gk2oRgds5DOizu9wLIJM_aC7GJlnGEK1KVkYmoI4S7U
17	2026-01-28 06:19:18+02	2026-01-27 06:19:25.768514+02	eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjbGllbnQxQGRlbW8uY29tIiwiaWF0IjoxNzY5NDg3NTU4LCJleHAiOjE3Njk1NzM5NTh9.o4q0RYmfscOVepqDgKvuxypIBPrZLkbRMrbvdF2JS8E
\.


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles (id, name) FROM stdin;
787d7aa2-a87c-4802-ab2c-0886f033c0b5	INSURANCE_CLIENT
90f4ee9f-bdbd-4cf8-8934-68a37e5e9011	DOCTOR
f7450117-cb68-4538-a649-e461e5e48448	PHARMACIST
6c39d263-9044-4676-a8fb-a07569fd845d	LAB_TECH
18abc6db-1fd4-4180-85aa-cf7573a0b77c	INSURANCE_MANAGER
62cde6f4-1869-42a4-9dd6-c386eb4d49c3	RADIOLOGIST
2e4f3359-f4ba-45a1-9e8a-2bbfb0986db5	MEDICAL_ADMIN
f919990c-2eaa-41f1-8a9c-17c372c88ed6	COORDINATION_ADMIN
\.


--
-- Data for Name: search_profiles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.search_profiles (id, address, clinic_registration, contact_info, description, id_or_passport_copy, location_lat, location_lng, medical_license, name, rejection_reason, status, type, university_degree, owner_id) FROM stdin;
fc24226a-0f3a-4e0b-b9c4-28d68784d0ad	رام الله - شارع الإرسال	\N	0592000001	أخصائي أمراض القلب والشرايين - خبرة 20 سنة	\N	31.9038	35.2034	\N	عيادة د. محمد القلب للقلب والأوعية الدموية	\N	APPROVED	DOCTOR	\N	95557a6b-92c4-4913-a9c0-8e3c35c67aad
eb23fb5a-aa5e-4743-a9ae-e9dc708f0213	رام الله - المصيون	\N	0592000002	أخصائية طب الأطفال وحديثي الولادة	\N	31.9065	35.201	\N	عيادة د. فاطمة الأطفال لطب الأطفال	\N	APPROVED	DOCTOR	\N	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23
f8f9bb0c-341b-40b3-8d4c-b75f809f854b	نابلس - شارع فيصل	\N	0592000003	أخصائي الطب الباطني والسكري	\N	32.2211	35.2544	\N	عيادة د. عمر الباطنة للأمراض الداخلية	\N	APPROVED	DOCTOR	\N	65ae7b21-d2bd-4685-a10c-76d406884d62
927a4647-20b2-42b3-ab54-033db5d3c616	نابلس - دوار الشهداء	\N	0592000004	أخصائية جراحة العيون والليزك	\N	32.223	35.256	\N	مركز د. نور لطب العيون	\N	APPROVED	DOCTOR	\N	434a147e-eaed-46fd-931b-d0ebaf4bcaea
45eb9f21-7008-42e8-bcab-7de18b60b73d	الخليل - باب الزاوية	\N	0592000005	أخصائي تجميل وزراعة الأسنان	\N	31.5326	35.0998	\N	عيادة د. ياسر لطب الأسنان	\N	APPROVED	DOCTOR	\N	8c9de48c-10af-4670-aadb-9f9ebb9e75a1
babfbc1d-b8e4-4bc4-b45d-99a7f996718f	بيت لحم - شارع النجمة	\N	0592000006	أخصائية أمراض النساء والتوليد	\N	31.7054	35.2024	\N	عيادة د. ريم للنساء والتوليد	\N	APPROVED	DOCTOR	\N	1c49d82a-0f7d-4b53-801e-74761df1008f
893132ba-5a69-4ba5-ab77-28d71f3c0b40	رام الله - البالوع	\N	0592000007	أخصائي جراحة العظام والمفاصل	\N	31.901	35.207	\N	مركز د. سامي لجراحة العظام	\N	APPROVED	DOCTOR	\N	c3ae6b3b-c835-448c-b0ad-298bec2c5cb3
0a6da52d-a9b9-4b61-9883-8a014ee586b0	نابلس - شارع سفيان	\N	0592000008	أخصائية الأمراض الجلدية والتجميل	\N	32.22	35.252	\N	عيادة د. لينا للأمراض الجلدية	\N	APPROVED	DOCTOR	\N	ae9f721f-4e97-4312-a680-daf79eaeba1f
845597ce-7b72-44df-85d0-fdbaf44536d5	رام الله - شارع الإرسال	\N	0593000001	صيدلية متكاملة - أدوية ومستحضرات طبية	\N	31.9045	35.204	\N	صيدلية الشفاء	\N	APPROVED	PHARMACY	\N	584a36c8-794c-42c6-9c09-e768dc32ee30
9628ce11-e404-430c-a43d-c28b6582ae77	نابلس - شارع فيصل	\N	0593000002	صيدلية على مدار الساعة	\N	32.222	35.255	\N	صيدلية الأمل	\N	APPROVED	PHARMACY	\N	01f4d329-987d-4560-98bf-298a3ece3f27
f6dd8685-a90e-40c0-9267-1add86a8001c	الخليل - باب الزاوية	\N	0593000003	صيدلية ومستودع أدوية	\N	31.533	35.1005	\N	صيدلية النور	\N	APPROVED	PHARMACY	\N	8f491018-1a64-4a6f-b9d6-c19e2f2e7c7c
86576cb2-03c9-4de3-b9c4-62501bce72ac	رام الله - المصيون	\N	0594000001	مختبر تحاليل طبية شامل - نتائج سريعة	\N	31.907	35.2	\N	مختبر الحياة الطبي	\N	APPROVED	LAB	\N	4f789b0d-9385-485e-b9c7-a586af5cabd1
76db9eaa-f7fd-40e7-83bc-e599c4a751cd	نابلس - دوار الشهداء	\N	0594000002	مختبر معتمد - جميع أنواع التحاليل	\N	32.2235	35.257	\N	مختبر الأمل للتحاليل	\N	APPROVED	LAB	\N	0760c6f6-4000-4aba-8357-86ef3e8f392a
fa05b05d-dfff-4cd5-acf3-b2f8a5f90c47	بيت لحم - شارع النجمة	\N	0594000003	مختبر تحاليل وفحوصات طبية	\N	31.706	35.203	\N	مختبر الصحة	\N	APPROVED	LAB	\N	b8b6de49-c402-4c8b-8320-5527e573992b
0799c9e7-393b-4c35-9415-844f5f98a5e6	رام الله - الماصيون	\N	0595000001	أشعة سينية - رنين مغناطيسي - أشعة مقطعية	\N	31.908	35.199	\N	مركز الأشعة الحديث	\N	APPROVED	RADIOLOGY	\N	48238880-3a07-4d63-8c1f-e7f5fc54da3b
63192321-4321-4834-b3b2-1b350a55dd48	نابلس - المخفية	\N	0595000002	مركز متكامل للتصوير الإشعاعي	\N	32.218	35.25	\N	مركز التصوير الطبي	\N	APPROVED	RADIOLOGY	\N	d2b04366-b548-40a8-957a-1adce0b9173e
bb945cc6-d498-404d-9beb-ad1c71721ef0	جنين - شارع الناصرة	\N	0592100001	فرع جديد لعيادة القلب	\N	32.4607	35.295	\N	مركز القلب الجديد - فرع جنين	{"reason":"a"}	REJECTED	DOCTOR	\N	95557a6b-92c4-4913-a9c0-8e3c35c67aad
59bc70e6-fcc3-4838-ba34-36caf7297150	طولكرم - شارع نابلس	\N	0594100001	فرع جديد للمختبر	\N	32.31	35.03	\N	مختبر الحياة - فرع طولكرم	{"reason":"a"}	REJECTED	LAB	\N	4f789b0d-9385-485e-b9c7-a586af5cabd1
\.


--
-- Data for Name: tests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.tests (id, created_at, test_name, union_price, updated_at) FROM stdin;
\.


--
-- Data for Name: v_role_id; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.v_role_id (id) FROM stdin;
5342bc8b-7238-47d2-871e-800c7a0fd9b2
\.


--
-- Data for Name: visits; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.visits (id, created_at, doctor_specialization, notes, updated_at, visit_date, visit_type, visit_year, doctor_id, family_member_id, patient_id, previous_visit_id) FROM stdin;
7f95c4a6-ab7e-43e4-863e-402ce102ab48	2026-01-27 05:39:17.844272+02	طب الأطفال	Diagnosis: الحمى\nTreatment: خافض حرارة	2026-01-27 05:39:17.844272+02	2026-01-27	NORMAL	2026	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	\N	20f27a3d-4257-438d-a327-ae327c0b29ce	\N
9f48e292-5b2a-4ba5-9414-b0a7a56499d9	2026-01-27 05:39:29.719417+02	طب الأطفال	Diagnosis: الحمى\nTreatment: خافض حرارة	2026-01-27 05:39:29.719417+02	2026-01-27	FOLLOW_UP	2026	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	\N	20f27a3d-4257-438d-a327-ae327c0b29ce	7f95c4a6-ab7e-43e4-863e-402ce102ab48
242fe9c3-a70d-4adf-a477-4e9c50d6c8bd	2026-01-27 05:41:07.726565+02	طب الأطفال	Diagnosis: التهاب الأذن\nTreatment: خافض حرارة	2026-01-27 05:41:07.726565+02	2026-01-27	FOLLOW_UP	2026	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	\N	20f27a3d-4257-438d-a327-ae327c0b29ce	9f48e292-5b2a-4ba5-9414-b0a7a56499d9
81697d0d-1c6d-49f9-9330-c20687334b2a	2026-01-27 05:49:20.966502+02	طب الأطفال	Diagnosis: الإسهال, الحمى, التهاب الأذن, التهاب الحلق, EMP011\nTreatment: dsfsdfsdfsdfsd	2026-01-27 05:49:20.966502+02	2026-01-27	FOLLOW_UP	2026	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	\N	20f27a3d-4257-438d-a327-ae327c0b29ce	242fe9c3-a70d-4adf-a477-4e9c50d6c8bd
39bf4a9e-43e5-431a-99d2-a34e072a4483	2026-01-27 05:55:09.241214+02	طب الأطفال	Diagnosis: التهاب الحلق\nTreatment: kj	2026-01-27 05:55:09.241214+02	2026-01-27	FOLLOW_UP	2026	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	\N	20f27a3d-4257-438d-a327-ae327c0b29ce	81697d0d-1c6d-49f9-9330-c20687334b2a
853a30cf-4e98-461d-8e78-204d90e7e748	2026-01-27 05:59:31.792284+02	طب الأطفال	Diagnosis: التهاب الأذن\nTreatment: asdasdasdas	2026-01-27 05:59:31.792284+02	2026-01-27	NORMAL	2026	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	5440c477-7bc1-48a3-aed4-0326a70ad712	\N	\N
323b2ba9-4d06-4803-918b-6722702dd28e	2026-01-27 06:07:44.83732+02	طب الأطفال	Diagnosis: التهاب الحلق\nTreatment: dsafas	2026-01-27 06:07:44.83732+02	2026-01-27	NORMAL	2026	2d3d6e1a-3d45-4f12-8ea6-7feac8180f23	3a2ebe1a-f7f4-4755-a2c1-7dbe438178cf	\N	\N
\.


--
-- Name: doctor_specialization_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.doctor_specialization_id_seq', 10, true);


--
-- Name: revoked_tokens_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.revoked_tokens_id_seq', 17, true);


--
-- Name: annual_usage annual_usage_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.annual_usage
    ADD CONSTRAINT annual_usage_pkey PRIMARY KEY (id);


--
-- Name: chronic_patient_schedules chronic_patient_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chronic_patient_schedules
    ADD CONSTRAINT chronic_patient_schedules_pkey PRIMARY KEY (id);


--
-- Name: claims claims_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.claims
    ADD CONSTRAINT claims_pkey PRIMARY KEY (id);


--
-- Name: client_roles client_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_roles
    ADD CONSTRAINT client_roles_pkey PRIMARY KEY (client_id, role_id);


--
-- Name: clients clients_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT clients_pkey PRIMARY KEY (id);


--
-- Name: conversations conversations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT conversations_pkey PRIMARY KEY (id);


--
-- Name: coverage_usage coverage_usage_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coverage_usage
    ADD CONSTRAINT coverage_usage_pkey PRIMARY KEY (id);


--
-- Name: coverages coverages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coverages
    ADD CONSTRAINT coverages_pkey PRIMARY KEY (id);


--
-- Name: doctor_medicine_assignments doctor_medicine_assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_medicine_assignments
    ADD CONSTRAINT doctor_medicine_assignments_pkey PRIMARY KEY (id);


--
-- Name: doctor_procedures doctor_procedures_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_procedures
    ADD CONSTRAINT doctor_procedures_pkey PRIMARY KEY (id);


--
-- Name: doctor_specialization doctor_specialization_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_specialization
    ADD CONSTRAINT doctor_specialization_pkey PRIMARY KEY (id);


--
-- Name: doctor_test_assignments doctor_test_assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_test_assignments
    ADD CONSTRAINT doctor_test_assignments_pkey PRIMARY KEY (id);


--
-- Name: emergency_requests emergency_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emergency_requests
    ADD CONSTRAINT emergency_requests_pkey PRIMARY KEY (id);


--
-- Name: family_members family_members_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.family_members
    ADD CONSTRAINT family_members_pkey PRIMARY KEY (id);


--
-- Name: healthcare_provider_claims healthcare_provider_claims_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.healthcare_provider_claims
    ADD CONSTRAINT healthcare_provider_claims_pkey PRIMARY KEY (id);


--
-- Name: lab_requests lab_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lab_requests
    ADD CONSTRAINT lab_requests_pkey PRIMARY KEY (id);


--
-- Name: medical_diagnoses medical_diagnoses_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medical_diagnoses
    ADD CONSTRAINT medical_diagnoses_pkey PRIMARY KEY (id);


--
-- Name: medical_records medical_records_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medical_records
    ADD CONSTRAINT medical_records_pkey PRIMARY KEY (id);


--
-- Name: medical_tests medical_tests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medical_tests
    ADD CONSTRAINT medical_tests_pkey PRIMARY KEY (id);


--
-- Name: medicine_prices medicine_prices_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medicine_prices
    ADD CONSTRAINT medicine_prices_pkey PRIMARY KEY (id);


--
-- Name: messages messages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: password_reset_tokens password_reset_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id);


--
-- Name: policies policies_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.policies
    ADD CONSTRAINT policies_pkey PRIMARY KEY (id);


--
-- Name: prescription_items prescription_items_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prescription_items
    ADD CONSTRAINT prescription_items_pkey PRIMARY KEY (id);


--
-- Name: prescriptions prescriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prescriptions
    ADD CONSTRAINT prescriptions_pkey PRIMARY KEY (id);


--
-- Name: price_list price_list_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.price_list
    ADD CONSTRAINT price_list_pkey PRIMARY KEY (id);


--
-- Name: provider_policies provider_policies_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.provider_policies
    ADD CONSTRAINT provider_policies_pkey PRIMARY KEY (id);


--
-- Name: radiology_requests radiology_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.radiology_requests
    ADD CONSTRAINT radiology_requests_pkey PRIMARY KEY (id);


--
-- Name: revoked_tokens revoked_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.revoked_tokens
    ADD CONSTRAINT revoked_tokens_pkey PRIMARY KEY (id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: search_profiles search_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search_profiles
    ADD CONSTRAINT search_profiles_pkey PRIMARY KEY (id);


--
-- Name: tests tests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tests
    ADD CONSTRAINT tests_pkey PRIMARY KEY (id);


--
-- Name: doctor_medicine_assignments uk6ktkigqqe3gtlw6cckeoeybwi; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_medicine_assignments
    ADD CONSTRAINT uk6ktkigqqe3gtlw6cckeoeybwi UNIQUE (doctor_id, medicine_id);


--
-- Name: family_members uk6uusbnsgol75t193gji8ecpsk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.family_members
    ADD CONSTRAINT uk6uusbnsgol75t193gji8ecpsk UNIQUE (national_id);


--
-- Name: password_reset_tokens uk71lqwbwtklmljk3qlsugr1mig; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT uk71lqwbwtklmljk3qlsugr1mig UNIQUE (token);


--
-- Name: coverage_usage uk8kslwiw8nr9qq05qyi5xbxdh6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coverage_usage
    ADD CONSTRAINT uk8kslwiw8nr9qq05qyi5xbxdh6 UNIQUE (client_id, provider_specialization, usage_date);


--
-- Name: coverages uk8qvyc0n60sqc4r7n3u83j7u89; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coverages
    ADD CONSTRAINT uk8qvyc0n60sqc4r7n3u83j7u89 UNIQUE (policy_id, service_name);


--
-- Name: provider_policies uk8uu3s87y6qyg0l9cdhxyx1lfj; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.provider_policies
    ADD CONSTRAINT uk8uu3s87y6qyg0l9cdhxyx1lfj UNIQUE (provider_id, service_name);


--
-- Name: clients uk9tpl6kc2cx19t73txqcej15db; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT uk9tpl6kc2cx19t73txqcej15db UNIQUE (national_id);


--
-- Name: family_members ukctu694c5o6odtbo5fgcemyron; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.family_members
    ADD CONSTRAINT ukctu694c5o6odtbo5fgcemyron UNIQUE (insurance_number);


--
-- Name: policies ukd27t4fo4735lcedivrfo0ie5i; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.policies
    ADD CONSTRAINT ukd27t4fo4735lcedivrfo0ie5i UNIQUE (policy_no);


--
-- Name: tests ukeun95fhgw0odg4ggweopwrn1; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tests
    ADD CONSTRAINT ukeun95fhgw0odg4ggweopwrn1 UNIQUE (test_name);


--
-- Name: clients ukg9dkcrvdpr9u3fryhkox294jx; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT ukg9dkcrvdpr9u3fryhkox294jx UNIQUE (employee_id);


--
-- Name: annual_usage ukhpe9uem1wp7cio9mlwid3pwkc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.annual_usage
    ADD CONSTRAINT ukhpe9uem1wp7cio9mlwid3pwkc UNIQUE (client_id, year, service_type);


--
-- Name: revoked_tokens uko7nu95tpd50oqhacdvs5qk1bf; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.revoked_tokens
    ADD CONSTRAINT uko7nu95tpd50oqhacdvs5qk1bf UNIQUE (token);


--
-- Name: roles ukofx66keruapi6vyqpv6f2or37; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT ukofx66keruapi6vyqpv6f2or37 UNIQUE (name);


--
-- Name: clients uksrv16ica2c1csub334bxjjb59; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT uksrv16ica2c1csub334bxjjb59 UNIQUE (email);


--
-- Name: doctor_test_assignments uktbrc29m3s0donph2ttv393qmb; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_test_assignments
    ADD CONSTRAINT uktbrc29m3s0donph2ttv393qmb UNIQUE (doctor_id, test_id);


--
-- Name: visits visits_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visits
    ADD CONSTRAINT visits_pkey PRIMARY KEY (id);


--
-- Name: idx_clients_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_clients_email ON public.clients USING btree (email);


--
-- Name: idx_dma_doctor; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dma_doctor ON public.doctor_medicine_assignments USING btree (doctor_id);


--
-- Name: idx_dma_medicine; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dma_medicine ON public.doctor_medicine_assignments USING btree (medicine_id);


--
-- Name: idx_dma_specialization; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dma_specialization ON public.doctor_medicine_assignments USING btree (specialization);


--
-- Name: idx_dta_doctor; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dta_doctor ON public.doctor_test_assignments USING btree (doctor_id);


--
-- Name: idx_dta_specialization; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dta_specialization ON public.doctor_test_assignments USING btree (specialization);


--
-- Name: idx_dta_test; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dta_test ON public.doctor_test_assignments USING btree (test_id);


--
-- Name: idx_dta_test_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dta_test_type ON public.doctor_test_assignments USING btree (test_type);


--
-- Name: idx_visit_doctor_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_visit_doctor_date ON public.visits USING btree (doctor_id, visit_date);


--
-- Name: idx_visit_family_member_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_visit_family_member_date ON public.visits USING btree (family_member_id, visit_date);


--
-- Name: idx_visit_patient_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_visit_patient_date ON public.visits USING btree (patient_id, visit_date);


--
-- Name: idx_visit_patient_year; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_visit_patient_year ON public.visits USING btree (patient_id, visit_year);


--
-- Name: coverages fk1eeuvnw4pvkj6l4cf33iq8tov; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coverages
    ADD CONSTRAINT fk1eeuvnw4pvkj6l4cf33iq8tov FOREIGN KEY (policy_id) REFERENCES public.policies(id);


--
-- Name: provider_policies fk1m3u9w70n41wy3qtntxf6om2y; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.provider_policies
    ADD CONSTRAINT fk1m3u9w70n41wy3qtntxf6om2y FOREIGN KEY (provider_id) REFERENCES public.clients(id);


--
-- Name: lab_requests fk1rffqggb6fa8csprlcuayi5rr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lab_requests
    ADD CONSTRAINT fk1rffqggb6fa8csprlcuayi5rr FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: notifications fk1xw62jvu0b0fem57a4gladb66; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fk1xw62jvu0b0fem57a4gladb66 FOREIGN KEY (sender_id) REFERENCES public.clients(id);


--
-- Name: client_roles fk20mdyn9gv0h2sauw6qkesfxom; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_roles
    ADD CONSTRAINT fk20mdyn9gv0h2sauw6qkesfxom FOREIGN KEY (client_id) REFERENCES public.clients(id);


--
-- Name: visits fk3kxgrlgagyfp6upkeufrthxfg; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visits
    ADD CONSTRAINT fk3kxgrlgagyfp6upkeufrthxfg FOREIGN KEY (previous_visit_id) REFERENCES public.visits(id);


--
-- Name: emergency_requests fk40kwl0h5i3wl6p7rl0avtw8md; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emergency_requests
    ADD CONSTRAINT fk40kwl0h5i3wl6p7rl0avtw8md FOREIGN KEY (member_id) REFERENCES public.clients(id);


--
-- Name: client_roles fk4o8ntxejbpn5quw4au89kmbwv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_roles
    ADD CONSTRAINT fk4o8ntxejbpn5quw4au89kmbwv FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: radiology_requests fk50fbtrjyhxeqiygs26ie4nejx; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.radiology_requests
    ADD CONSTRAINT fk50fbtrjyhxeqiygs26ie4nejx FOREIGN KEY (member_id) REFERENCES public.clients(id);


--
-- Name: family_member_documents fk52646ajui2rptvi6d5qsbqgvo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.family_member_documents
    ADD CONSTRAINT fk52646ajui2rptvi6d5qsbqgvo FOREIGN KEY (family_member_id) REFERENCES public.family_members(id);


--
-- Name: doctor_test_assignments fk562hq0uxpdk98yptew8ig4lk3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_test_assignments
    ADD CONSTRAINT fk562hq0uxpdk98yptew8ig4lk3 FOREIGN KEY (test_id) REFERENCES public.medical_tests(id);


--
-- Name: doctor_test_assignments fk5yri2wb5jl8wvwam7vacrom9j; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_test_assignments
    ADD CONSTRAINT fk5yri2wb5jl8wvwam7vacrom9j FOREIGN KEY (assigned_by) REFERENCES public.clients(id);


--
-- Name: prescriptions fk6371p076t2y5lic6mdt8q01pp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prescriptions
    ADD CONSTRAINT fk6371p076t2y5lic6mdt8q01pp FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: doctor_medicine_assignments fk656dosx2c7kd8ghavd6668c8g; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_medicine_assignments
    ADD CONSTRAINT fk656dosx2c7kd8ghavd6668c8g FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: prescription_items fk6uh7tdy2lv6sx34u1365acqsf; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prescription_items
    ADD CONSTRAINT fk6uh7tdy2lv6sx34u1365acqsf FOREIGN KEY (prescription_id) REFERENCES public.prescriptions(id);


--
-- Name: radiology_requests fk78excs6dv3ykf0a26x65ugwy2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.radiology_requests
    ADD CONSTRAINT fk78excs6dv3ykf0a26x65ugwy2 FOREIGN KEY (radiologist_id) REFERENCES public.clients(id);


--
-- Name: radiology_requests fk7pq145t1rkyfmi1yq30t36x59; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.radiology_requests
    ADD CONSTRAINT fk7pq145t1rkyfmi1yq30t36x59 FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: client_doctor_documents fk7rv0fh3my20l6t0pya5va0m8p; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_doctor_documents
    ADD CONSTRAINT fk7rv0fh3my20l6t0pya5va0m8p FOREIGN KEY (client_id) REFERENCES public.clients(id);


--
-- Name: doctor_test_assignments fk8s10tn0lvymlb86xufs0jclmq; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_test_assignments
    ADD CONSTRAINT fk8s10tn0lvymlb86xufs0jclmq FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: claims fk939bdcedi2vql56c3b5xa4vis; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.claims
    ADD CONSTRAINT fk939bdcedi2vql56c3b5xa4vis FOREIGN KEY (member_id) REFERENCES public.clients(id);


--
-- Name: conversations fk9hb7wet212ewsyrhkm0d31nhx; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT fk9hb7wet212ewsyrhkm0d31nhx FOREIGN KEY (user2_id) REFERENCES public.clients(id);


--
-- Name: visits fk9k5sig9v4lyeql9j4w7jgvbe8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visits
    ADD CONSTRAINT fk9k5sig9v4lyeql9j4w7jgvbe8 FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: messages fka8yuuiu8ih0w6oggnc7jbtv5q; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fka8yuuiu8ih0w6oggnc7jbtv5q FOREIGN KEY (receiver_id) REFERENCES public.clients(id);


--
-- Name: price_list_allowed_specializations fkamy68liyh3hr9v0dlr5qooxgo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.price_list_allowed_specializations
    ADD CONSTRAINT fkamy68liyh3hr9v0dlr5qooxgo FOREIGN KEY (specialization_id) REFERENCES public.doctor_specialization(id);


--
-- Name: doctor_medicine_assignments fkbma90pbxeiisirgr5dja9lwvl; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_medicine_assignments
    ADD CONSTRAINT fkbma90pbxeiisirgr5dja9lwvl FOREIGN KEY (assigned_by) REFERENCES public.clients(id);


--
-- Name: visits fkbtgdrpb7t9o3crxcaqi7ehvdx; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visits
    ADD CONSTRAINT fkbtgdrpb7t9o3crxcaqi7ehvdx FOREIGN KEY (patient_id) REFERENCES public.clients(id);


--
-- Name: search_profiles fkcu7um5fc6no2vbqllqj09581t; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search_profiles
    ADD CONSTRAINT fkcu7um5fc6no2vbqllqj09581t FOREIGN KEY (owner_id) REFERENCES public.clients(id);


--
-- Name: lab_requests fkcun9cdxcato9jqx6fj9b5nuk5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lab_requests
    ADD CONSTRAINT fkcun9cdxcato9jqx6fj9b5nuk5 FOREIGN KEY (member_id) REFERENCES public.clients(id);


--
-- Name: price_list_allowed_genders fkcve5cfs3v0ka07fu7o8jykhcr; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.price_list_allowed_genders
    ADD CONSTRAINT fkcve5cfs3v0ka07fu7o8jykhcr FOREIGN KEY (price_list_id) REFERENCES public.price_list(id);


--
-- Name: conversations fkddlpoqm3rdfbkv7jd9r1pg2wk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT fkddlpoqm3rdfbkv7jd9r1pg2wk FOREIGN KEY (user1_id) REFERENCES public.clients(id);


--
-- Name: doctor_specialization_allowed_genders fkfi0sf5yk23lt0jdtmmk8pd0y6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_specialization_allowed_genders
    ADD CONSTRAINT fkfi0sf5yk23lt0jdtmmk8pd0y6 FOREIGN KEY (specialization_id) REFERENCES public.doctor_specialization(id);


--
-- Name: healthcare_provider_claims fkfweswl5utabmgee2haxrf48dg; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.healthcare_provider_claims
    ADD CONSTRAINT fkfweswl5utabmgee2haxrf48dg FOREIGN KEY (provider_id) REFERENCES public.clients(id);


--
-- Name: messages fkhldlthynlnp1d04ftgn4xvddp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fkhldlthynlnp1d04ftgn4xvddp FOREIGN KEY (sender_id) REFERENCES public.clients(id);


--
-- Name: family_members fkihjs36r9w66us9vwyaar9kqux; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.family_members
    ADD CONSTRAINT fkihjs36r9w66us9vwyaar9kqux FOREIGN KEY (client_id) REFERENCES public.clients(id);


--
-- Name: price_list_allowed_specializations fkj4ffj7t0puwpglt5oet4ui5pk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.price_list_allowed_specializations
    ADD CONSTRAINT fkj4ffj7t0puwpglt5oet4ui5pk FOREIGN KEY (price_list_id) REFERENCES public.price_list(id);


--
-- Name: chronic_patient_schedules fkjfeibcx5qb5xwlagp9opt4lvn; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chronic_patient_schedules
    ADD CONSTRAINT fkjfeibcx5qb5xwlagp9opt4lvn FOREIGN KEY (patient_id) REFERENCES public.clients(id);


--
-- Name: notifications fkjm7q7l8p5npt0c3cusfi898pj; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fkjm7q7l8p5npt0c3cusfi898pj FOREIGN KEY (recipient_id) REFERENCES public.clients(id);


--
-- Name: healthcare_provider_claims fkjnhpiqg4ytyaavvud6wml5y5l; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.healthcare_provider_claims
    ADD CONSTRAINT fkjnhpiqg4ytyaavvud6wml5y5l FOREIGN KEY (policy_id) REFERENCES public.policies(id);


--
-- Name: medical_records fklg3kpf7sme9ko5f4h2t5ofoy5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medical_records
    ADD CONSTRAINT fklg3kpf7sme9ko5f4h2t5ofoy5 FOREIGN KEY (doctor_id) REFERENCES public.clients(id);


--
-- Name: claims fkm0w2xffwe13pmkusoxnxuim7j; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.claims
    ADD CONSTRAINT fkm0w2xffwe13pmkusoxnxuim7j FOREIGN KEY (policy_id) REFERENCES public.policies(id);


--
-- Name: lab_requests fkmjcvdaupmu9k57ea6pg57932x; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lab_requests
    ADD CONSTRAINT fkmjcvdaupmu9k57ea6pg57932x FOREIGN KEY (price_id) REFERENCES public.price_list(id);


--
-- Name: clients fkmvac0bkdq2xnk1cblpvw5aiyh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT fkmvac0bkdq2xnk1cblpvw5aiyh FOREIGN KEY (policy_id) REFERENCES public.policies(id);


--
-- Name: lab_requests fknjc98od4ejkcgcya6gtibo4xk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lab_requests
    ADD CONSTRAINT fknjc98od4ejkcgcya6gtibo4xk FOREIGN KEY (lab_tech_id) REFERENCES public.clients(id);


--
-- Name: prescription_items fkooqpbcfdxsl8kpmytgyu7indp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prescription_items
    ADD CONSTRAINT fkooqpbcfdxsl8kpmytgyu7indp FOREIGN KEY (price_list_id) REFERENCES public.price_list(id);


--
-- Name: prescriptions fkpwyw9am7es9nqydvqh8e15578; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prescriptions
    ADD CONSTRAINT fkpwyw9am7es9nqydvqh8e15578 FOREIGN KEY (member_id) REFERENCES public.clients(id);


--
-- Name: medical_records fkqsw9jdc6wey67mfjo9ldx6mkg; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.medical_records
    ADD CONSTRAINT fkqsw9jdc6wey67mfjo9ldx6mkg FOREIGN KEY (member_id) REFERENCES public.clients(id);


--
-- Name: coverage_usage fkrdbvqhbas13s2rc4pfeinv7nc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coverage_usage
    ADD CONSTRAINT fkrdbvqhbas13s2rc4pfeinv7nc FOREIGN KEY (client_id) REFERENCES public.clients(id);


--
-- Name: radiology_requests fkrn7vjlmof2cpfbt0bnd58dw0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.radiology_requests
    ADD CONSTRAINT fkrn7vjlmof2cpfbt0bnd58dw0 FOREIGN KEY (price_id) REFERENCES public.price_list(id);


--
-- Name: annual_usage fks3f1dnkyjj4t9prbvk7gbrdfd; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.annual_usage
    ADD CONSTRAINT fks3f1dnkyjj4t9prbvk7gbrdfd FOREIGN KEY (client_id) REFERENCES public.clients(id);


--
-- Name: client_university_cards fksppsg3e5lyg5sr6w7unssrny1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_university_cards
    ADD CONSTRAINT fksppsg3e5lyg5sr6w7unssrny1 FOREIGN KEY (client_id) REFERENCES public.clients(id);


--
-- Name: messages fkt492th6wsovh1nush5yl5jj8e; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fkt492th6wsovh1nush5yl5jj8e FOREIGN KEY (conversation_id) REFERENCES public.conversations(id);


--
-- Name: prescriptions fkt5puwvjok2nfm3400c5bf01ql; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prescriptions
    ADD CONSTRAINT fkt5puwvjok2nfm3400c5bf01ql FOREIGN KEY (pharmacist_id) REFERENCES public.clients(id);


--
-- Name: visits fkt9ut7lemcm7jgab8d304bjrp6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.visits
    ADD CONSTRAINT fkt9ut7lemcm7jgab8d304bjrp6 FOREIGN KEY (family_member_id) REFERENCES public.family_members(id);


--
-- Name: client_chronic_documents fktawyg5x5kik281uou72bgpke5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_chronic_documents
    ADD CONSTRAINT fktawyg5x5kik281uou72bgpke5 FOREIGN KEY (client_id) REFERENCES public.clients(id);


--
-- Name: client_chronic_diseases fkthwhu1qkayrthummgrk6hqg83; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.client_chronic_diseases
    ADD CONSTRAINT fkthwhu1qkayrthummgrk6hqg83 FOREIGN KEY (client_id) REFERENCES public.clients(id);


--
-- Name: doctor_medicine_assignments fktk4kg5ctu4lce8552xhgpk5wd; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.doctor_medicine_assignments
    ADD CONSTRAINT fktk4kg5ctu4lce8552xhgpk5wd FOREIGN KEY (medicine_id) REFERENCES public.medicine_prices(id);


--
-- PostgreSQL database dump complete
--

\unrestrict avRup4CkZYV20M4b0PuxUbGBmCItPRDQ4HICuYFBAeEIgp2CJNTcY7TP8YKAN51

