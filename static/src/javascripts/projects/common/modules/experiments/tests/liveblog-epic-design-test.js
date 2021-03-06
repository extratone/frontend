// @flow
import { getSync as geolocationGetSync } from 'lib/geolocation';
import config from 'lib/config';
import {setupEpicInLiveblog} from "common/modules/commercial/contributions-liveblog-utilities";
import {
    isCompatibleWithLiveBlogEpic,
    liveBlogTemplate,
    makeEpicABTest,
    buildEpicCopy,
} from "common/modules/commercial/contributions-utilities";

const geolocation = geolocationGetSync();

const copyGlobal: AcquisitionsEpicTemplateCopy = {
    paragraphs: [
        'In these extraordinary times, the Guardian’s editorial independence has never been more important. Because no one sets our agenda, or edits our editor, we can keep delivering quality, trustworthy, fact-checked journalism each and every day. Free from commercial or political influence, we can report fearlessly on world events and challenge those in power.',
        'Your support protects the Guardian’s independence. We believe every one of us deserves equal access to accurate news and calm explanation. No matter how unpredictable the future feels, we will remain with you, delivering high quality news so we can all make critical decisions about our lives, health and security – based on fact, not fiction.',
        'Support the Guardian from as little as %%CURRENCY_SYMBOL%%1 – and it only takes a minute. Thank you.',
    ],
};

const copyUS: AcquisitionsEpicTemplateCopy = {
    paragraphs: [
        '<b>The real work is just beginning … </b>',
        '… following Joe Biden’s victory in the 2020 election. A majority of American voters have rejected four years of divisiveness, racism and sustained assaults on constitutional democracy. They have chosen a new path. But America’s systemic challenges remain.',
        'The Guardian welcomes the opportunity to refocus our journalism on the opportunities that lie ahead for America – from fixing a broken healthcare system, to repairing global alliances, to addressing wealth inequality and corrosive racial bias.',
        'We believe every one of us deserves equal access to fact-based news and analysis. That’s why we’ve decided to keep Guardian journalism free for all readers, regardless of where they live or what they can afford to pay.',
        'If you’ve enjoyed our live updates and in-depth coverage, support the Guardian from as little as %%CURRENCY_SYMBOL%%1 – and it only takes a minute. Thank you.',
    ],
};

const copy = geolocation === 'US' ? copyUS : copyGlobal;

export const liveblogEpicDesignTest: EpicABTest = makeEpicABTest({
    id: 'LiveblogEpicDesignTestR2',
    campaignId: 'liveblog-epic-design-test-r2',

    geolocation,
    highPriority: false,

    start: '2020-10-15',
    expiry: '2021-01-27',

    author: 'TF',
    description: 'Test designs for the liveblog epic',
    successMeasure: 'Conversion rate',
    idealOutcome: 'Acquires many Supporters',

    audienceCriteria: 'All',
    audience: 1,
    audienceOffset: 0,
    canRun: () => config.get('page').contentType === 'LiveBlog',
    pageCheck: isCompatibleWithLiveBlogEpic,
    deploymentRules: 'AlwaysAsk',

    variants: [
        {
            id: 'control',
            products: ['CONTRIBUTION', 'MEMBERSHIP_SUPPORTER'],
            test: setupEpicInLiveblog,
            template: liveBlogTemplate('liveblog-epic-test__control'),
            copy: buildEpicCopy(copy, false, geolocation),
        },
        {
            id: 'v1',
            products: ['CONTRIBUTION', 'MEMBERSHIP_SUPPORTER'],
            test: setupEpicInLiveblog,
            template: liveBlogTemplate('liveblog-epic-test__v1'),
            copy: buildEpicCopy(copy, false, geolocation),
        },
        {
            id: 'v2',
            products: ['CONTRIBUTION', 'MEMBERSHIP_SUPPORTER'],
            test: setupEpicInLiveblog,
            template: liveBlogTemplate('liveblog-epic-test__v2'),
            copy: buildEpicCopy(copy, false, geolocation),
        },
    ],
});
