// @flow

import config from 'lib/config';

let isInTest;

const isInServerSideTest = (): boolean =>
    config.get('tests.useAusCmpVariant', false) === 'variant';

export const isInAusCmpTest = (): boolean => {
    if (typeof isInTest === 'undefined') {
        isInTest = isInServerSideTest();
    }

    return isInTest;
};
