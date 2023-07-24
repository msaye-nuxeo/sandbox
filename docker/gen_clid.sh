#!/bin/bash

# ======================================================================

  # Generate NUXEO_CLID and add to the .env file

  set -e; # Exit immediately if a pipeline, list, or compound command exits with a non-zero status

  set -f; # Disable pathname expansion

# set -u; # Treat unset variables and parameters other than the special parameters "@" and "*" as an error when performing parameter expansion

# set -x; #  After expanding each simple command, for command, case command, select command, or arithmetic for command, display the expanded value of PS4, followed by the command and its expanded arguments or associated word list.

# ======================================================================

envGet () {

# echo "envGet()" >&2

  local _KEY="$1"; shift
  local _ENV="$1"; shift
  local _DEF="$1"; shift

# echo "_KEY=[${_KEY}] _ENV=[${_ENV}] _DEF=[${_DEF}]" >&2

  local _VAL=$(grep "^${_KEY}=" < "${_ENV}" | tail -n 1 | cut -d= -f2 | tr -d '\r\n')
# echo "_KEY=[${_KEY}] _VAL=[${_VAL}]" >&2

  if (test -z "${_VAL}"); then
    _VAL="${_DEF}"
#   echo "_KEY=[${_KEY}] _VAL=[${_VAL}]" >&2
  fi

  echo "${_VAL}"; return 0

}

# ======================================================================

  DC_ENV=".env"; # Docker Compose environment file

  ERR=""

  NUXEO_CLID_VAR="NUXEO_CLI"

# ======================================================================

  NUXEO_CLID=$(envGet "NUXEO_CLID" "${DC_ENV}")
  if [ -n "${NUXEO_CLID}" ]; then
    echo "'NUXEO_CLID' appears to be configured in ${DC_ENV}. Please remove and run this script again."
    exit 2
  fi

# ======================================================================

  INSTANCE_DESC=$(envGet "INSTANCE_DESC" "${DC_ENV}" "Nuxeo Development")
  INSTANCE_TYPE=$(envGet "INSTANCE_TYPE" "${DC_ENV}" "dev")

  APP_NAME=$(envGet "APP_NAME" "${DC_ENV}")
  if [ -z "${APP_NAME}" ]; then
    echo "'APP_NAME' (project name) is not set, please configure ${DC_ENV}"
    ERR="yes"
  fi

  NUXEO_IMAGE=$(envGet "NUXEO_IMAGE" "${DC_ENV}")
  if [ -z "${NUXEO_IMAGE}" ]; then
    NUXEO_IMAGE="docker-private.packages.nuxeo.com/nuxeo/nuxeo:2021"
    echo "'NUXEO_IMAGE' is not set in ${DC_ENV}, using: ${NUXEO_IMAGE}"
  fi

  STUDIO_PROJECT=$(envGet "STUDIO_PROJECT" "${DC_ENV}")
# echo "1 STUDIO_PROJECT='${STUDIO_PROJECT}'"
# echo "1 STUDIO_PROJECT=[${STUDIO_PROJECT}]"
  if [ -z "${STUDIO_PROJECT}" ]; then
#   echo "set STUDIO_PROJECT from ARG1"
    STUDIO_PROJECT="$1"
#   echo "2 STUDIO_PROJECT='${STUDIO_PROJECT}'"
  fi
  if [ -z "${STUDIO_PROJECT}" ]; then
    echo "'STUDIO_PROJECT' is not set, please configure ${DC_ENV} or provide as argument 1"
    ERR="yes"
  fi

  STUDIO_USERNAME=$(envGet "STUDIO_USERNAME" "${DC_ENV}")
  if [ -z "${STUDIO_USERNAME}" ]; then
    echo "'STUDIO_USERNAME' is not set, please configure ${DC_ENV}"
    ERR="yes"
  fi

  # https://connect.nuxeo.com/nuxeo/site/connect/tokens
  STUDIO_TOKEN=$(envGet "STUDIO_TOKEN" "${DC_ENV}")
  if [ -z "${STUDIO_TOKEN}" ]; then
    echo "'STUDIO_TOKEN' is not set, please configure in ${DC_ENV}"
    ERR="yes"
  fi

  if [ -n "${ERR}" ]; then
    echo "ERROR"
    exit 1
  fi

  if [ -n "$2" ]; then
    NUXEO_CLID_VAR="$2"
  fi

# ======================================================================

  # Create temp directory
  TMP_DIR=$(mktemp -d)
  chmod 777 ${TMP_DIR}

  # Register an instance with Nuxeo Online Services
  # nuxeoctl register [<username> [<project> [<type> <description>] [<pwd>]]]
  # https://doc.nuxeo.com/nxdoc/nuxeoctl-and-control-panel-usage/#per-command-usage

  echo "nuxeoctl register '${STUDIO_USERNAME}' '${STUDIO_PROJECT}' '${INSTANCE_TYPE}' '${INSTANCE_DESC}' '<STUDIO_TOKEN>'"

  docker run --rm -v ${TMP_DIR}:/var/lib/nuxeo/:rw ${NUXEO_IMAGE} \
    nuxeoctl register "${STUDIO_USERNAME}" "${STUDIO_PROJECT}" "${INSTANCE_TYPE}" "${INSTANCE_DESC}" "${STUDIO_TOKEN}"

  NUXEO_CLID="${TMP_DIR}/instance.clid"
  echo "NUXEO_CLID='${NUXEO_CLID}'"

  # Write CLID to env file
  if [ -f "${NUXEO_CLID}" ]; then
    echo -n "${NUXEO_CLID_VAR}=" >> "${DC_ENV}"
    awk 1 ORS="--" ${NUXEO_CLID} >> "${DC_ENV}"
    echo "" >> "${DC_ENV}"
  else
    echo "Could not find NUXEO_CLID '${NUXEO_CLID}'"
    exit 2
  fi

  # Remove temp directory
  rm -rfv "${TMP_DIR}"

# ======================================================================
